package com.groupsoft.piedrazul.user.application.exception;

import com.groupsoft.piedrazul.shared.exception.DomainException;

import java.util.Map;

public class RegistrationException extends DomainException {

    private final Map<String, String> fields;

    public RegistrationException(String code, String message, Map<String, String> fields) {
        super(code, message);
        this.fields = fields == null ? Map.of() : Map.copyOf(fields);
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public static RegistrationException validation(Map<String, String> fields) {
        return new RegistrationException(
                "VALIDATION_ERROR",
                "Revisa los datos ingresados.",
                fields
        );
    }

    public static RegistrationException conflict(String field, String message) {
        return new RegistrationException("DUPLICATE", message, Map.of(field, message));
    }
}
