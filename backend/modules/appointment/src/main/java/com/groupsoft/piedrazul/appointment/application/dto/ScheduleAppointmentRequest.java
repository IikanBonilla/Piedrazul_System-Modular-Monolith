package com.groupsoft.piedrazul.appointment.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScheduleAppointmentRequest {
    private String documentNumber;
    private Long doctorId;
    private LocalDateTime slot;
    private Boolean confirmed;
    private String notes;
}
