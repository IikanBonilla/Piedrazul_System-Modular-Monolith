package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentResponseDTO;
import com.groupsoft.piedrazul.appointment.application.dto.AppointmentSearchResultDTO;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSearchException;
import com.groupsoft.piedrazul.appointment.application.mapper.AppointmentAssembler;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import com.groupsoft.piedrazul.shared.port.DoctorQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * HE-01 / HU-1.1, HU-1.2, HU-1.3
 * SRP: este caso de uso solo orquesta la busqueda de citas por medico y fecha.
 */
@Service
@RequiredArgsConstructor
public class SearchAppointmentsByDoctorAndDateUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final DoctorQueryPort doctorQueryPort;
    private final AppointmentAssembler appointmentAssembler;

    @Transactional(readOnly = true)
    public AppointmentSearchResultDTO execute(Long doctorId, LocalDate date) {
        validateSearchCriteria(doctorId, date);

        var doctor = doctorQueryPort.findById(doctorId)
                .orElseThrow(() -> AppointmentSearchException.doctorNotFound(doctorId));

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<AppointmentResponseDTO> appointments = appointmentRepository
                .findByDoctorAndDateRange(doctorId, start, end)
                .stream()
                .sorted(Comparator.comparing(a -> a.getAppointmentDate()))
                .map(appointmentAssembler::toResponse)
                .toList();

        return AppointmentSearchResultDTO.builder()
                .doctorId(doctorId)
                .doctorName(doctor.fullName())
                .date(date)
                .total(appointments.size())
                .appointments(appointments)
                .build();
    }

    private void validateSearchCriteria(Long doctorId, LocalDate date) {
        if (doctorId == null) {
            throw AppointmentSearchException.missingDoctor();
        }
        if (date == null) {
            throw AppointmentSearchException.missingDate();
        }
    }
}
