package com.groupsoft.piedrazul.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupsoft.piedrazul.user.application.exception.RegistrationException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakIdentityProvisionerTest {

    private HttpServer server;

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void createsTheUserAndAssignsTheRealmRole() throws Exception {
        AtomicReference<String> createdBody = new AtomicReference<>();
        AtomicReference<String> roleBody = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            if (path.endsWith("/protocol/openid-connect/token")) {
                send(exchange, 200, "{\"access_token\":\"admin-token\"}");
                return;
            }
            if ("POST".equals(method) && path.endsWith("/users")) {
                createdBody.set(new String(requestBody, StandardCharsets.UTF_8));
                exchange.getResponseHeaders().add("Location", base() + "/admin/realms/piedrazul/users/user-1");
                send(exchange, 201, "");
                return;
            }
            if ("GET".equals(method) && path.endsWith("/roles/PATIENT")) {
                send(exchange, 200, "{\"id\":\"role-1\",\"name\":\"PATIENT\"}");
                return;
            }
            if ("POST".equals(method) && path.endsWith("/role-mappings/realm")) {
                roleBody.set(new String(requestBody, StandardCharsets.UTF_8));
                send(exchange, 204, "");
                return;
            }
            send(exchange, 500, "");
        });
        server.start();

        provisioner().provision("ana", "ana@example.com", "Ana Ruiz", "clave1234", "PATIENT");

        assertTrue(createdBody.get().contains("\"username\":\"ana\""));
        assertTrue(createdBody.get().contains("\"value\":\"clave1234\""));
        assertTrue(roleBody.get().contains("\"name\":\"PATIENT\""));
    }

    @Test
    void rejectsADuplicatedKeycloakUser() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.endsWith("/protocol/openid-connect/token")) {
                send(exchange, 200, "{\"access_token\":\"admin-token\"}");
                return;
            }
            send(exchange, 409, "");
        });
        server.start();

        RegistrationException error = assertThrows(
                RegistrationException.class,
                () -> provisioner().provision("ana", "ana@example.com", "Ana Ruiz", "clave1234", "PATIENT")
        );
        assertEquals("DUPLICATE", error.getCode());
    }

    private KeycloakIdentityProvisioner provisioner() {
        return new KeycloakIdentityProvisioner(
                new ObjectMapper(),
                java.net.http.HttpClient.newHttpClient(),
                base(),
                "piedrazul",
                "admin",
                "admin"
        );
    }

    private String base() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private void send(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws java.io.IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, payload.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(payload);
        }
    }
}
