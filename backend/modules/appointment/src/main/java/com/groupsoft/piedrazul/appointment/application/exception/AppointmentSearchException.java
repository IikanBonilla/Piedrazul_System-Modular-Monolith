package com.groupsoft.piedrazul.appointment.application.exception;

import com.groupsoft.piedrazul.shared.exception.DomainException;

public class AppointmentSearchException extends DomainException {

    public AppointmentSearchException(String code, String message) {
        super(code, message);
    }

    public static AppointmentSearchException missingDoctor() {
        return new AppointmentSearchException(
                "MISSING_DOCTOR",
                "Debe seleccionar un medico o terapista antes de realizar la busqueda."
        );
    }

    public static AppointmentSearchException missingDate() {
        return new AppointmentSearchException(
                "MISSING_DATE",
                "Debe seleccionar una fecha antes de realizar la busqueda."
        );
    }

    public static AppointmentSearchException doctorNotFound(Long doctorId) {
        return new AppointmentSearchException(
                "DOCTOR_NOT_FOUND",
                "No existe un medico o terapista con id: " + doctorId
        );
    }
}
