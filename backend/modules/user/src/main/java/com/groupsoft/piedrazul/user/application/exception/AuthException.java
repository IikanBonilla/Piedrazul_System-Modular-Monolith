package com.groupsoft.piedrazul.user.application.exception;

import com.groupsoft.piedrazul.shared.exception.DomainException;

import java.util.Map;

public class AuthException extends DomainException {

    private final Map<String, String> fields;

    public AuthException(String code, String message) {
        this(code, message, Map.of());
    }

    public AuthException(String code, String message, Map<String, String> fields) {
        super(code, message);
        this.fields = fields == null ? Map.of() : Map.copyOf(fields);
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public static AuthException invalidCredentials() {
        return new AuthException(
                "INVALID_CREDENTIALS",
                "Usuario o contraseña incorrectos."
        );
    }

    public static AuthException inactive() {
        return new AuthException(
                "USER_INACTIVE",
                "Esta cuenta está inactiva. Contacta al administrador."
        );
    }

    public static AuthException validation(Map<String, String> fields) {
        return new AuthException(
                "VALIDATION_ERROR",
                "Revisa los datos ingresados.",
                fields
        );
    }
}
