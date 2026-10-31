package com.groupsoft.piedrazul.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Traduce los roles del access token de Keycloak a autoridades de Spring.
 * Acepta {@code realm_access.roles}, el claim plano {@code roles} y
 * {@code resource_access.<cliente>.roles}.
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    public static final List<String> APPLICATION_ROLES = List.of(
            "ADMINISTRATOR",
            "SCHEDULER",
            "DOCTOR",
            "PATIENT"
    );

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (String role : extractRoles(jwt)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return new JwtAuthenticationToken(jwt, authorities, username(jwt));
    }

    /**
     * Rol de negocio con el que la interfaz elige la pantalla de inicio.
     * Si el token trae varios, gana el de mayor privilegio.
     */
    public static String primaryApplicationRole(Jwt jwt) {
        Set<String> roles = extractRoles(jwt);
        for (String candidate : APPLICATION_ROLES) {
            if (roles.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    public static String username(Jwt jwt) {
        if (jwt == null) {
            return "";
        }
        String preferred = jwt.getClaimAsString("preferred_username");
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        return jwt.getSubject() == null ? "" : jwt.getSubject();
    }

    static Set<String> extractRoles(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();
        if (jwt == null) {
            return roles;
        }
        addAll(roles, jwt.getClaim("roles"));
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> realmMap) {
            addAll(roles, realmMap.get("roles"));
        }
        Object resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess instanceof Map<?, ?> resources) {
            for (Object client : resources.values()) {
                if (client instanceof Map<?, ?> clientMap) {
                    addAll(roles, clientMap.get("roles"));
                }
            }
        }
        return roles;
    }

    private static void addAll(Set<String> target, Object source) {
        if (!(source instanceof Collection<?> values)) {
            return;
        }
        for (Object value : values) {
            if (value instanceof String role && !role.isBlank()) {
                target.add(role);
            }
        }
    }
}
