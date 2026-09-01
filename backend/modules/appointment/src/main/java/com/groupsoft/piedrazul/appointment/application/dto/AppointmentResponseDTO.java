package com.groupsoft.piedrazul.appointment.application.dto;

import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class AppointmentResponseDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientDocument;
    private Long doctorId;
    private String doctorName;
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
    private String whatsappNumber;
    private String notes;
}
