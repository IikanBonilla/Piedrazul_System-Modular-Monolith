package com.groupsoft.piedrazul.appointment.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScheduleAppointmentRequest {
    private Long patientId;
    private Long doctorId;
    private LocalDateTime slot;
}
