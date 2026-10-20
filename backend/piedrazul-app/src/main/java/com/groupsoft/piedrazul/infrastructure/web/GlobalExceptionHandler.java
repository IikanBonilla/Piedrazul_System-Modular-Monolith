package com.groupsoft.piedrazul.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupsoft.piedrazul.shared.exception.DomainException;
import com.groupsoft.piedrazul.user.application.exception.AuthException;
import com.groupsoft.piedrazul.user.application.exception.RegistrationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String UNAUTHORIZED_MESSAGE = "Debes iniciar sesión.";
    private static final String EXPIRED_MESSAGE = "Tu sesión expiró. Ingresa de nuevo.";
    private static final String FORBIDDEN_MESSAGE = "No tienes permiso para acceder a esta función.";

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        if (isExpired(authException)) {
            write(response, HttpStatus.UNAUTHORIZED, "SESSION_EXPIRED", EXPIRED_MESSAGE);
            return;
        }
        write(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", UNAUTHORIZED_MESSAGE);
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        write(response, HttpStatus.FORBIDDEN, "FORBIDDEN", FORBIDDEN_MESSAGE);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return body(HttpStatus.FORBIDDEN, "FORBIDDEN", FORBIDDEN_MESSAGE, Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        if (isExpired(ex)) {
            return body(HttpStatus.UNAUTHORIZED, "SESSION_EXPIRED", EXPIRED_MESSAGE, Map.of());
        }
        return body(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", UNAUTHORIZED_MESSAGE, Map.of());
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(AuthException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case "INVALID_CREDENTIALS", "UNAUTHORIZED", "SESSION_EXPIRED" -> HttpStatus.UNAUTHORIZED;
            case "USER_INACTIVE", "FORBIDDEN", "ACCOUNT_NOT_LINKED" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
        return body(status, ex.getCode(), ex.getMessage(), ex.getFields());
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Map<String, Object>> handleRegistration(RegistrationException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case "DUPLICATE" -> HttpStatus.CONFLICT;
            case "IDENTITY_PROVIDER" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
        return body(status, ex.getCode(), ex.getMessage(), ex.getFields());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, Object>> handleDomainException(DomainException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case "MISSING_DOCTOR", "MISSING_DATE", "MISSING_SLOT", "MISSING_PATIENT",
                 "INCOMPLETE_REGISTRATION", "CONFIRMATION_REQUIRED",
                 "INVALID_BOOKING_WINDOW", "NO_WORKING_DAYS",
                 "INVALID_TIME_RANGE", "INVALID_SLOT_INTERVAL" -> HttpStatus.BAD_REQUEST;
            case "DOCTOR_NOT_FOUND", "PATIENT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "USER_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        return body(status, ex.getCode(), ex.getMessage(), Map.of());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> handleUnreadable(Exception ex) {
        return body(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Algunos datos no tienen un formato válido. Revisa la fecha y vuelve a intentar.",
                Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex) {
        return body(
                HttpStatus.CONFLICT,
                "DUPLICATE",
                "No se pudo guardar porque algunos datos ya están registrados.",
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        log.error("Error no controlado", ex);
        return body(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Ocurrió un error en el servidor. Intenta de nuevo más tarde.",
                Map.of()
        );
    }

    private ResponseEntity<Map<String, Object>> body(
            HttpStatus status,
            String code,
            String message,
            Map<String, String> fields
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", code);
        payload.put("message", message);
        if (fields != null && !fields.isEmpty()) {
            payload.put("fields", fields);
        }
        return ResponseEntity.status(status).body(payload);
    }

    private void write(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body(status, code, message, Map.of()).getBody());
    }

    private boolean isExpired(Exception exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains("expired")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
