package com.groupsoft.piedrazul.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    private static final Set<String> STAFF_ROLES = Set.of("ADMINISTRATOR", "SCHEDULER", "DOCTOR");

    private final AuthTokenService authTokenService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/register")) {
            return true;
        }
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui.html")) {
            return true;
        }
        return !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            write(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Debes iniciar sesión.");
            return;
        }

        IssuedToken issued = authTokenService.resolve(header.substring(7).trim()).orElse(null);
        if (issued == null) {
            write(response, HttpServletResponse.SC_UNAUTHORIZED, "SESSION_EXPIRED",
                    "Tu sesión expiró. Ingresa de nuevo.");
            return;
        }

        if (!isAllowed(request.getMethod(), request.getRequestURI(), issued.role())) {
            write(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN",
                    "No tienes permiso para acceder a esta función.");
            return;
        }

        request.setAttribute("authUserId", issued.userId());
        request.setAttribute("authRole", issued.role());
        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String method, String path, String role) {
        if (path.startsWith("/api/v1/admin/")) {
            return "ADMINISTRATOR".equals(role);
        }
        if (path.startsWith("/api/v1/appointments/doctor")) {
            return STAFF_ROLES.contains(role);
        }
        if ("POST".equalsIgnoreCase(method) && "/api/v1/appointments".equals(path)) {
            return "PATIENT".equals(role) || STAFF_ROLES.contains(role);
        }
        if (path.startsWith("/api/v1/doctors")) {
            return "PATIENT".equals(role) || STAFF_ROLES.contains(role);
        }
        return true;
    }

    private void write(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        Map<String, String> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
