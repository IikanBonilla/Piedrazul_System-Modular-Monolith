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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * HE-02 / HU-2.1: agendamiento de cita por el paciente.
 */
@Service
@RequiredArgsConstructor
public class ScheduleAppointmentUseCase {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MAX_NOTES_LENGTH = 300;
    private static final Pattern CONTROL_CHARS = Pattern.compile("\\p{Cntrl}");
    private static final Set<AppointmentStatus> OCCUPYING_STATUSES = EnumSet.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.COMPLETED
    );

    private final AppointmentRepositoryPort appointmentRepository;
    private final DoctorQueryPort doctorQueryPort;
    private final UserQueryPort userQueryPort;
    private final DoctorSchedulePort doctorSchedulePort;

    /**
     * El paciente es siempre el de la sesión ({@code patientId}); el documento del cuerpo,
     * si viene, solo se compara contra él y nunca se usa para buscar a otro paciente.
     */
    @Transactional
    public ScheduleAppointmentConfirmationDTO execute(Long patientId, ScheduleAppointmentRequest request) {
        ScheduleAppointmentRequest safeRequest = request == null
                ? ScheduleAppointmentRequest.builder().build()
                : request;

        validate(safeRequest);
        String notes = normalizeNotes(safeRequest.getNotes());

        UserSummary patient = userQueryPort.findById(patientId)
                .orElseThrow(() -> AppointmentSchedulingException.patientNotFound(String.valueOf(patientId)));
        ensureSamePatient(safeRequest.getDocumentNumber(), patient);

        DoctorSummary doctor = doctorQueryPort.findById(safeRequest.getDoctorId())
                .orElseThrow(() -> AppointmentSchedulingException.doctorNotFound(safeRequest.getDoctorId()));

        LocalDateTime slot = safeRequest.getSlot();
        LocalDate date = slot.toLocalDate();
        List<LocalDateTime> candidates = doctorSchedulePort.candidateSlots(doctor.id(), date);
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
                .notes(notes)
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

    /**
     * Se compara antes de cualquier consulta por cédula: un documento ajeno responde
     * igual exista o no, así no se puede sondear qué cédulas están registradas.
     */
    private void ensureSamePatient(String requestedDocument, UserSummary patient) {
        if (requestedDocument == null || requestedDocument.isBlank()) {
            return;
        }
        if (!requestedDocument.trim().equals(patient.documentNumber())) {
            throw AppointmentSchedulingException.patientMismatch();
        }
    }

    private String normalizeNotes(String notes) {
        if (notes == null) {
            return null;
        }
        String trimmed = notes.trim();
        if (CONTROL_CHARS.matcher(notes).find() || trimmed.length() > MAX_NOTES_LENGTH) {
            throw AppointmentSchedulingException.invalidNotes();
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validate(ScheduleAppointmentRequest request) {
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
