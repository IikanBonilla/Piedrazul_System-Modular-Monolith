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

    public static AppointmentSearchException missingDocument() {
        return new AppointmentSearchException(
                "MISSING_DOCUMENT",
                "Ingresa la cédula del paciente."
        );
    }

    public static AppointmentSearchException invalidDocument() {
        return new AppointmentSearchException(
                "INVALID_DOCUMENT",
                "La cédula debe tener entre 6 y 15 dígitos, sin letras ni espacios."
        );
    }

    public static AppointmentSearchException patientNotFound() {
        return new AppointmentSearchException(
                "PATIENT_NOT_FOUND",
                "No hay un paciente activo con esa cédula."
        );
    }
}
