package com.groupsoft.piedrazul.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerSecurityTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(new ObjectMapper());

    @Test
    void missingTokenResponds401() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(
                new MockHttpServletRequest(),
                response,
                new InsufficientAuthenticationException("Full authentication is required")
        );

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"UNAUTHORIZED\""));
        assertTrue(response.getContentAsString().contains("Debes iniciar sesión."));
    }

    @Test
    void expiredTokenResponds401SessionExpired() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.commence(
                new MockHttpServletRequest(),
                response,
                new InsufficientAuthenticationException("Jwt expired at 2026-10-19T00:00:00Z")
        );

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"SESSION_EXPIRED\""));
    }

    @Test
    void deniedRoleResponds403() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("denied"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"FORBIDDEN\""));
        assertTrue(response.getContentAsString().contains("No tienes permiso"));
    }
}
