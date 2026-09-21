package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.shared.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, String>> handleDomainException(DomainException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case "MISSING_DOCTOR", "MISSING_DATE", "MISSING_SLOT", "MISSING_PATIENT",
                 "INCOMPLETE_REGISTRATION", "CONFIRMATION_REQUIRED" -> HttpStatus.BAD_REQUEST;
            case "DOCTOR_NOT_FOUND", "PATIENT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "USER_ALREADY_EXISTS" -> HttpStatus.CONFLICT;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };

        return ResponseEntity.status(status).body(Map.of(
                "code", ex.getCode(),
                "message", ex.getMessage()
        ));
    }
}
