package com.groupsoft.piedrazul.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.groupsoft.piedrazul.user.application.exception.RegistrationException;
import com.groupsoft.piedrazul.user.application.port.IdentityProvisioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Component
public class KeycloakIdentityProvisioner implements IdentityProvisioner {

    private static final Logger log = LoggerFactory.getLogger(KeycloakIdentityProvisioner.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String realm;
    private final String adminUsername;
    private final String adminPassword;

    public KeycloakIdentityProvisioner(
            ObjectMapper objectMapper,
            @Value("${piedrazul.keycloak.base-url}") String baseUrl,
            @Value("${piedrazul.keycloak.realm}") String realm,
            @Value("${piedrazul.keycloak.admin-username}") String adminUsername,
            @Value("${piedrazul.keycloak.admin-password}") String adminPassword
    ) {
        this(
                objectMapper,
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
                baseUrl,
                realm,
                adminUsername,
                adminPassword
        );
    }

    KeycloakIdentityProvisioner(
            ObjectMapper objectMapper,
            HttpClient httpClient,
            String baseUrl,
            String realm,
            String adminUsername,
            String adminPassword
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.realm = realm;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void provision(String username, String email, String fullName, String rawPassword, String role) {
        try {
            String token = adminToken();
            String userId = createUser(token, username, email, fullName, rawPassword);
            try {
                assignRole(token, userId, role);
            } catch (RuntimeException ex) {
                deleteUser(token, userId);
                throw ex;
            }
        } catch (RegistrationException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Provision de Keycloak interrumpida para {}", username);
            throw RegistrationException.identityProvider();
        } catch (IOException ex) {
            log.warn("No se pudo contactar a Keycloak para crear {}", username);
            throw RegistrationException.identityProvider();
        }
    }

    @Override
    public void rollback(String username) {
        try {
            String token = adminToken();
            findUserId(token, username).ifPresent(userId -> deleteUser(token, userId));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Rollback de Keycloak interrumpido para {}", username);
        } catch (IOException | RuntimeException ex) {
            log.warn("No se pudo revertir el usuario {} en Keycloak", username);
        }
    }

    private String adminToken() throws IOException, InterruptedException {
        String form = "grant_type=password&client_id=admin-cli&username="
                + encode(adminUsername) + "&password=" + encode(adminPassword);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/realms/master/protocol/openid-connect/token"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            log.warn("Keycloak rechazo el token de administracion con estado {}", response.statusCode());
            throw RegistrationException.identityProvider();
        }
        JsonNode body = objectMapper.readTree(response.body());
        String accessToken = body.path("access_token").asText("");
        if (accessToken.isBlank()) {
            throw RegistrationException.identityProvider();
        }
        return accessToken;
    }

    private String createUser(
            String token,
            String username,
            String email,
            String fullName,
            String rawPassword
    ) throws IOException, InterruptedException {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("username", username);
        payload.put("email", email);
        payload.put("enabled", true);
        payload.put("emailVerified", true);
        applyName(payload, fullName);
        ArrayNode credentials = payload.putArray("credentials");
        ObjectNode credential = credentials.addObject();
        credential.put("type", "password");
        credential.put("value", rawPassword);
        credential.put("temporary", false);

        HttpRequest request = authorized(token, "/users")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 409) {
            throw RegistrationException.conflict("username", "Este usuario ya está registrado.");
        }
        if (response.statusCode() != 201) {
            log.warn("Keycloak no creo el usuario {} (estado {})", username, response.statusCode());
            throw RegistrationException.identityProvider();
        }
        Optional<String> created = userIdFromLocation(response);
        if (created.isPresent()) {
            return created.get();
        }
        return findUserId(token, username).orElseThrow(RegistrationException::identityProvider);
    }

    private void assignRole(String token, String userId, String role) throws IOException, InterruptedException {
        HttpRequest readRole = authorized(token, "/roles/" + encodePath(role)).GET().build();
        HttpResponse<String> roleResponse = httpClient.send(readRole, HttpResponse.BodyHandlers.ofString());
        if (roleResponse.statusCode() != 200) {
            log.warn("El rol {} no existe en el realm (estado {})", role, roleResponse.statusCode());
            throw RegistrationException.identityProvider();
        }
        JsonNode roleNode = objectMapper.readTree(roleResponse.body());
        ArrayNode roles = objectMapper.createArrayNode();
        ObjectNode assigned = roles.addObject();
        assigned.put("id", roleNode.path("id").asText());
        assigned.put("name", roleNode.path("name").asText(role));

        HttpRequest mapRole = authorized(token, "/users/" + userId + "/role-mappings/realm")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(roles)))
                .build();
        HttpResponse<String> mapped = httpClient.send(mapRole, HttpResponse.BodyHandlers.ofString());
        if (mapped.statusCode() != 204 && mapped.statusCode() != 200) {
            log.warn("No se asigno el rol {} al usuario {} (estado {})", role, userId, mapped.statusCode());
            throw RegistrationException.identityProvider();
        }
    }

    private Optional<String> findUserId(String token, String username) throws IOException, InterruptedException {
        HttpRequest request = authorized(token, "/users?username=" + encode(username) + "&exact=true").GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return Optional.empty();
        }
        JsonNode users = objectMapper.readTree(response.body());
        if (!users.isArray() || users.isEmpty()) {
            return Optional.empty();
        }
        String id = users.get(0).path("id").asText("");
        return id.isBlank() ? Optional.empty() : Optional.of(id);
    }

    private void deleteUser(String token, String userId) {
        try {
            HttpRequest request = authorized(token, "/users/" + userId).DELETE().build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            log.warn("No se pudo borrar el usuario {} en Keycloak", userId);
        }
    }

    private HttpRequest.Builder authorized(String token, String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/admin/realms/" + realm + path))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + token);
    }

    private Optional<String> userIdFromLocation(HttpResponse<String> response) {
        return response.headers().firstValue("Location").flatMap(location -> {
            int slash = location.lastIndexOf('/');
            if (slash < 0 || slash == location.length() - 1) {
                return Optional.empty();
            }
            return Optional.of(location.substring(slash + 1));
        });
    }

    private void applyName(ObjectNode payload, String fullName) {
        String trimmed = fullName == null ? "" : fullName.trim();
        int split = trimmed.indexOf(' ');
        if (split < 0) {
            payload.put("firstName", trimmed);
            payload.put("lastName", trimmed);
            return;
        }
        payload.put("firstName", trimmed.substring(0, split));
        payload.put("lastName", trimmed.substring(split + 1).trim());
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String encodePath(String value) {
        return encode(value).replace("+", "%20");
    }

    private String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
