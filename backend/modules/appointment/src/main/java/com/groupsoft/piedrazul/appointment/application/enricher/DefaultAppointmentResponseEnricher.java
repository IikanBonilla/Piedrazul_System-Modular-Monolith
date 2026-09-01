package com.groupsoft.piedrazul.appointment.application.enricher;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentResponseDTO;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.shared.port.DoctorQueryPort;
import com.groupsoft.piedrazul.shared.port.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultAppointmentResponseEnricher implements AppointmentResponseEnricher {

    private final UserQueryPort userQueryPort;
    private final DoctorQueryPort doctorQueryPort;

    @Override
    public AppointmentResponseDTO enrich(Appointment appointment, AppointmentResponseDTO baseDto) {
        var builder = baseDto.toBuilder();

        userQueryPort.findById(appointment.getPatientId()).ifPresent(user -> {
            builder.patientName(user.fullName());
            builder.patientDocument(user.documentNumber());
        });

        doctorQueryPort.findById(appointment.getDoctorId()).ifPresent(doctor -> {
            builder.doctorName(doctor.fullName());
        });

        return builder.build();
    }
}
