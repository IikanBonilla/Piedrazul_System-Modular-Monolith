package com.groupsoft.piedrazul.appointment.application.exception;

import com.groupsoft.piedrazul.shared.exception.DomainException;

public class AppointmentSchedulingException extends DomainException {

    public AppointmentSchedulingException(String code, String message) {
        super(code, message);
    }

    public static AppointmentSchedulingException missingDoctor() {
        return new AppointmentSchedulingException(
                "MISSING_DOCTOR",
                "Debe seleccionar un medico o terapista antes de consultar disponibilidad."
        );
    }

    public static AppointmentSchedulingException missingDate() {
        return new AppointmentSchedulingException(
                "MISSING_DATE",
                "Debe seleccionar una fecha antes de consultar disponibilidad."
        );
    }

    public static AppointmentSchedulingException doctorNotFound(Long doctorId) {
        return new AppointmentSchedulingException(
                "DOCTOR_NOT_FOUND",
                "No existe un medico o terapista con id: " + doctorId
        );
    }

    public static AppointmentSchedulingException missingSlot() {
        return new AppointmentSchedulingException(
                "MISSING_SLOT",
                "Debe seleccionar una franja horaria disponible."
        );
    }

    public static AppointmentSchedulingException slotNotAvailable() {
        return new AppointmentSchedulingException(
                "SLOT_NOT_AVAILABLE",
                "La franja seleccionada no esta disponible."
        );
    }
}
