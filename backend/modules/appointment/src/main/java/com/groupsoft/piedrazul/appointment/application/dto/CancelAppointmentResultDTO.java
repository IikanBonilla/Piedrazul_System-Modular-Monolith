package com.groupsoft.piedrazul.appointment.application.dto;

public record CancelAppointmentResultDTO(
        Long id,
        String status,
        String message
) {
}
