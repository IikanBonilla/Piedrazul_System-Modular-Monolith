package com.groupsoft.piedrazul.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void mapsRealmRolesToSpringAuthorities() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(jwt(
                Map.of("roles", List.of("PATIENT", "offline_access")),
                List.of("PATIENT")
        ));

        assertEquals("paciente", authentication.getName());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PATIENT")));
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_offline_access")));
    }

    @Test
    void prefersTheHighestApplicationRole() {
        Jwt jwt = jwt(Map.of("roles", List.of("PATIENT", "ADMINISTRATOR")), Map.of());

        assertEquals("ADMINISTRATOR", KeycloakRealmRoleConverter.primaryApplicationRole(jwt));
    }

    @Test
    void returnsNullWhenTheTokenHasNoApplicationRole() {
        Jwt jwt = jwt(Map.of(), Map.of("roles", List.of("offline_access")));

        assertNull(KeycloakRealmRoleConverter.primaryApplicationRole(jwt));
    }

    private Jwt jwt(Map<String, Object> realmAccess, Object flatRoles) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("subject-1")
                .claim("preferred_username", "paciente")
                .claim("realm_access", realmAccess)
                .claim("roles", flatRoles)
                .issuedAt(Instant.parse("2026-10-19T00:00:00Z"))
                .expiresAt(Instant.parse("2026-10-19T08:00:00Z"))
                .build();
    }
}
