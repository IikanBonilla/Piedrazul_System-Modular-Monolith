package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.ScheduleAppointmentConfirmationDTO;
import com.groupsoft.piedrazul.appointment.application.dto.ScheduleAppointmentRequest;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSchedulingException;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import com.groupsoft.piedrazul.shared.dto.DoctorSummary;
import com.groupsoft.piedrazul.shared.dto.UserSummary;
import com.groupsoft.piedrazul.shared.port.DoctorQueryPort;
import com.groupsoft.piedrazul.shared.port.DoctorSchedulePort;
import com.groupsoft.piedrazul.shared.port.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HE-02 / HU-2.1: agendamiento de cita por el paciente.
 */
@Service
@RequiredArgsConstructor
public class ScheduleAppointmentUseCase {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final Set<AppointmentStatus> OCCUPYING_STATUSES = EnumSet.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.COMPLETED
    );

    private final AppointmentRepositoryPort appointmentRepository;
    private final DoctorQueryPort doctorQueryPort;
    private final UserQueryPort userQueryPort;
    private final DoctorSchedulePort doctorSchedulePort;

    @Transactional
    public ScheduleAppointmentConfirmationDTO execute(ScheduleAppointmentRequest request) {
        ScheduleAppointmentRequest safeRequest = request == null
                ? ScheduleAppointmentRequest.builder().build()
                : request;

        validate(safeRequest);

        String documentNumber = safeRequest.getDocumentNumber().trim();
        UserSummary patient = userQueryPort.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> AppointmentSchedulingException.patientNotFound(documentNumber));

        DoctorSummary doctor = doctorQueryPort.findById(safeRequest.getDoctorId())
                .orElseThrow(() -> AppointmentSchedulingException.doctorNotFound(safeRequest.getDoctorId()));

        LocalDateTime slot = safeRequest.getSlot();
        LocalDate date = slot.toLocalDate();
        List<LocalDateTime> candidates = doctorSchedulePort.candidateSlots(date);
        Set<LocalDateTime> occupied = occupiedSlots(doctor.id(), date);
        List<LocalDateTime> available = candidates.stream()
                .filter(candidate -> !occupied.contains(candidate))
                .toList();

        if (available.isEmpty()) {
            throw AppointmentSchedulingException.noAvailableSlots();
        }
        if (!available.contains(slot)) {
            throw AppointmentSchedulingException.slotNotAvailable();
        }

        Appointment saved = appointmentRepository.save(Appointment.builder()
                .patientId(patient.id())
                .doctorId(doctor.id())
                .appointmentDate(slot)
                .status(AppointmentStatus.CONFIRMED)
                .whatsappNumber(patient.phone())
                .notes(safeRequest.getNotes())
                .build());

        String confirmation = "Cita agendada exitosamente para el "
                + slot.format(DAY_FORMAT)
                + " a las "
                + slot.format(TIME_FORMAT)
                + " con "
                + doctor.fullName()
                + ".";

        return ScheduleAppointmentConfirmationDTO.builder()
                .id(saved.getId())
                .patientName(patient.fullName())
                .patientDocument(patient.documentNumber())
                .doctorName(doctor.fullName())
                .appointmentDate(saved.getAppointmentDate())
                .status(saved.getStatus())
                .message(confirmation)
                .build();
    }

    private Set<LocalDateTime> occupiedSlots(Long doctorId, LocalDate date) {
        return appointmentRepository
                .findByDoctorAndDateRange(doctorId, date.atStartOfDay(), date.atTime(23, 59, 59))
                .stream()
                .filter(appointment -> OCCUPYING_STATUSES.contains(appointment.getStatus()))
                .map(Appointment::getAppointmentDate)
                .collect(Collectors.toSet());
    }

    private void validate(ScheduleAppointmentRequest request) {
        if (request.getDocumentNumber() == null || request.getDocumentNumber().isBlank()) {
            throw AppointmentSchedulingException.missingPatient();
        }
        if (request.getDoctorId() == null) {
            throw AppointmentSchedulingException.missingDoctor();
        }
        if (request.getSlot() == null) {
            throw AppointmentSchedulingException.missingSlot();
        }
        if (!Boolean.TRUE.equals(request.getConfirmed())) {
            throw AppointmentSchedulingException.confirmationRequired();
        }
    }
}
