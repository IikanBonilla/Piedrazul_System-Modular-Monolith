package com.groupsoft.piedrazul.appointment.application.exception;

import com.groupsoft.piedrazul.shared.exception.DomainException;

public class AppointmentSearchException extends DomainException {

    public AppointmentSearchException(String code, String message) {
        super(code, message);
    }

    public static AppointmentSearchException missingDoctor() {
        return new AppointmentSearchException(
                "MISSING_DOCTOR",
                "Selecciona un médico o terapista antes de buscar."
        );
    }

    public static AppointmentSearchException missingDate() {
        return new AppointmentSearchException(
                "MISSING_DATE",
                "Selecciona una fecha antes de buscar."
        );
    }

    public static AppointmentSearchException doctorNotFound(Long doctorId) {
        return new AppointmentSearchException(
                "DOCTOR_NOT_FOUND",
                "No existe el médico o terapista seleccionado."
        );
    }
}
