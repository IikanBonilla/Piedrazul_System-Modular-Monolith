package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentResponseDTO;
import com.groupsoft.piedrazul.appointment.application.dto.PatientAppointmentHistoryDTO;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSearchException;
import com.groupsoft.piedrazul.appointment.application.mapper.AppointmentAssembler;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import com.groupsoft.piedrazul.shared.dto.UserSummary;
import com.groupsoft.piedrazul.shared.port.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Historial de citas de un paciente a partir de su cédula.
 */
@Service
@RequiredArgsConstructor
public class SearchAppointmentsByPatientDocumentUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final UserQueryPort userQueryPort;
    private final AppointmentAssembler appointmentAssembler;

    @Transactional(readOnly = true)
    public PatientAppointmentHistoryDTO execute(String documentNumber) {
        String normalized = normalizeDocument(documentNumber);
        UserSummary patient = userQueryPort.findByDocumentNumber(normalized)
                .orElseThrow(AppointmentSearchException::patientNotFound);

        List<AppointmentResponseDTO> appointments = appointmentRepository
                .findByPatientId(patient.id())
                .stream()
                .map(appointmentAssembler::toResponse)
                .toList();

        return PatientAppointmentHistoryDTO.builder()
                .patientId(patient.id())
                .patientName(patient.fullName())
                .patientDocument(patient.documentNumber())
                .total(appointments.size())
                .appointments(appointments)
                .build();
    }

    private String normalizeDocument(String documentNumber) {
        if (documentNumber == null || documentNumber.isBlank()) {
            throw AppointmentSearchException.missingDocument();
        }
        String normalized = documentNumber.trim();
        if (!normalized.matches("\\d{6,15}")) {
            throw AppointmentSearchException.invalidDocument();
        }
        return normalized;
    }
}
