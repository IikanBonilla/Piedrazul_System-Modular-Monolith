package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentResponseDTO;
import com.groupsoft.piedrazul.appointment.application.dto.PatientAppointmentHistoryDTO;
import com.groupsoft.piedrazul.appointment.application.mapper.AppointmentAssembler;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import com.groupsoft.piedrazul.shared.port.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListMyAppointmentsUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final UserQueryPort userQueryPort;
    private final AppointmentAssembler appointmentAssembler;

    @Transactional(readOnly = true)
    public PatientAppointmentHistoryDTO execute(Long patientId) {
        List<AppointmentResponseDTO> appointments = appointmentRepository
                .findByPatientId(patientId)
                .stream()
                .map(appointmentAssembler::toResponse)
                .toList();

        var patient = userQueryPort.findById(patientId).orElse(null);
        return PatientAppointmentHistoryDTO.builder()
                .patientId(patientId)
                .patientName(patient == null ? null : patient.fullName())
                .patientDocument(patient == null ? null : patient.documentNumber())
                .total(appointments.size())
                .appointments(appointments)
                .build();
    }
}
