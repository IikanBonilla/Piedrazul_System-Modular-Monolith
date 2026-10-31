package com.groupsoft.piedrazul.availability.application.exception;

import com.groupsoft.piedrazul.shared.exception.DomainException;

public class InvalidSchedulingConfigException extends DomainException {

    public InvalidSchedulingConfigException(String code, String message) {
        super(code, message);
    }

    public static InvalidSchedulingConfigException invalidBookingWindow() {
        return new InvalidSchedulingConfigException(
                "INVALID_BOOKING_WINDOW",
                "La ventana de semanas debe ser un numero entero mayor a 0."
        );
    }

    public static InvalidSchedulingConfigException noWorkingDays() {
        return new InvalidSchedulingConfigException(
                "NO_WORKING_DAYS",
                "Debe seleccionar al menos un dia de atencion."
        );
    }

    public static InvalidSchedulingConfigException invalidTimeRange() {
        return new InvalidSchedulingConfigException(
                "INVALID_TIME_RANGE",
                "La hora de inicio debe ser anterior a la hora de fin en cada franja."
        );
    }

    public static InvalidSchedulingConfigException invalidSlotInterval() {
        return new InvalidSchedulingConfigException(
                "INVALID_SLOT_INTERVAL",
                "El intervalo entre citas debe ser un numero entero mayor a 0."
        );
    }

    public static InvalidSchedulingConfigException missingTimeSlots() {
        return new InvalidSchedulingConfigException(
                "INVALID_TIME_RANGE",
                "Debe configurar al menos una franja horaria con hora de inicio y fin."
        );
    }

    public static InvalidSchedulingConfigException doctorNotFound(Long doctorId) {
        return new InvalidSchedulingConfigException(
                "DOCTOR_NOT_FOUND",
                "No existe un medico o terapista con id: " + doctorId
        );
    }
}
