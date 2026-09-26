package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.shared.exception.DomainException;
import com.groupsoft.piedrazul.user.application.exception.AuthException;
import com.groupsoft.piedrazul.user.application.exception.RegistrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(AuthException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case "INVALID_CREDENTIALS", "UNAUTHORIZED", "SESSION_EXPIRED" -> HttpStatus.UNAUTHORIZED;
            case "USER_INACTIVE", "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
        return body(status, ex.getCode(), ex.getMessage(), ex.getFields());
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Map<String, Object>> handleRegistration(RegistrationException ex) {
        HttpStatus status = "DUPLICATE".equals(ex.getCode()) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return body(status, ex.getCode(), ex.getMessage(), ex.getFields());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, Object>> handleDomainException(DomainException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case "MISSING_DOCTOR", "MISSING_DATE" -> HttpStatus.BAD_REQUEST;
            case "DOCTOR_NOT_FOUND" -> HttpStatus.NOT_FOUND;
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
}
