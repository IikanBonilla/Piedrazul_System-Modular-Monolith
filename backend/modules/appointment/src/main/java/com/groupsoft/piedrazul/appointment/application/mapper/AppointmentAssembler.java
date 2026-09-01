package com.groupsoft.piedrazul.appointment.application.mapper;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentResponseDTO;
import com.groupsoft.piedrazul.appointment.application.enricher.AppointmentResponseEnricher;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentAssembler {

    private final AppointmentResponseEnricher enricher;

    public AppointmentResponseDTO toResponse(Appointment appointment) {
        AppointmentResponseDTO base = AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .appointmentDate(appointment.getAppointmentDate())
                .status(appointment.getStatus())
                .whatsappNumber(appointment.getWhatsappNumber())
                .notes(appointment.getNotes())
                .build();

        return enricher.enrich(appointment, base);
    }
}
