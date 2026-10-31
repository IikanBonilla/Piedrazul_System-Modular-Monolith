package com.groupsoft.piedrazul.appointment.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PatientAppointmentHistoryDTO {
    private Long patientId;
    private String patientName;
    private String patientDocument;
    private int total;
    private List<AppointmentResponseDTO> appointments;
}
