package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.CancelAppointmentResultDTO;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSchedulingException;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CancelAppointmentUseCase {

    private static final Set<AppointmentStatus> CANCELLABLE = EnumSet.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED
    );

    private final AppointmentRepositoryPort appointmentRepository;

    @Transactional
    public CancelAppointmentResultDTO execute(
            Long patientId,
            Long appointmentId,
            String justification,
            LocalDateTime now
    ) {
        String reason = normalizeJustification(justification);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(AppointmentSchedulingException::appointmentNotFound);

        if (patientId == null || !patientId.equals(appointment.getPatientId())) {
            throw AppointmentSchedulingException.notOwner();
        }
        if (!CANCELLABLE.contains(appointment.getStatus())) {
            throw AppointmentSchedulingException.notCancellable();
        }
        LocalDate today = now.toLocalDate();
        if (appointment.getAppointmentDate().toLocalDate().isBefore(today.plusDays(2))) {
            throw AppointmentSchedulingException.tooLateToCancel();
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason);
        appointmentRepository.save(appointment);

        return new CancelAppointmentResultDTO(
                appointment.getId(),
                AppointmentStatus.CANCELLED.name(),
                "La cita quedó cancelada y el horario volvió a quedar disponible."
        );
    }

    private String normalizeJustification(String justification) {
        if (justification == null || justification.isBlank()) {
            throw AppointmentSchedulingException.missingJustification();
        }
        String normalized = justification.trim().replaceAll("\\s+", " ");
        if (normalized.length() < 10 || normalized.length() > 300) {
            throw AppointmentSchedulingException.invalidJustification();
        }
        return normalized;
    }
}
