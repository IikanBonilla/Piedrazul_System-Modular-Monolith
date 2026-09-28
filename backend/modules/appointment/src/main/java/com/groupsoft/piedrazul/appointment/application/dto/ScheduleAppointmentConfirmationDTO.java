package com.groupsoft.piedrazul.appointment.application.dto;

import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScheduleAppointmentConfirmationDTO {
    private Long id;
    private String patientName;
    private String patientDocument;
    private String doctorName;
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
    private String message;
}
