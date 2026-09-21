package com.groupsoft.piedrazul.user.application.exception;

import com.groupsoft.piedrazul.shared.exception.DomainException;

import java.util.List;

public class PatientRegistrationException extends DomainException {

    public PatientRegistrationException(String code, String message) {
        super(code, message);
    }

    public static PatientRegistrationException incomplete(List<String> missingFields) {
        return new PatientRegistrationException(
                "INCOMPLETE_REGISTRATION",
                "Debe completar los campos obligatorios: " + String.join(", ", missingFields) + "."
        );
    }

    public static PatientRegistrationException alreadyExists() {
        return new PatientRegistrationException(
                "USER_ALREADY_EXISTS",
                "El usuario ya existe. No se puede registrar un duplicado."
        );
    }
}
