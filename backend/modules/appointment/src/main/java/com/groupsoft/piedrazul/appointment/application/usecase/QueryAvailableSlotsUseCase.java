package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.AvailableSlotsResultDTO;
import com.groupsoft.piedrazul.appointment.application.dto.TimeSlotDTO;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSchedulingException;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import com.groupsoft.piedrazul.shared.port.DoctorQueryPort;
import com.groupsoft.piedrazul.shared.port.DoctorSchedulePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HE-02 / HU-2.3 + HE-03 / HU-3.5: franjas disponibles segun config del administrador.
 */
@Service
@RequiredArgsConstructor
public class QueryAvailableSlotsUseCase {

    private static final Set<AppointmentStatus> OCCUPYING_STATUSES = EnumSet.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.COMPLETED
    );

    private final AppointmentRepositoryPort appointmentRepository;
    private final DoctorQueryPort doctorQueryPort;
    private final DoctorSchedulePort doctorSchedulePort;

    @Transactional(readOnly = true)
    public AvailableSlotsResultDTO execute(Long doctorId, LocalDate date) {
        if (doctorId == null) {
            throw AppointmentSchedulingException.missingDoctor();
        }
        if (date == null) {
            throw AppointmentSchedulingException.missingDate();
        }

        var doctor = doctorQueryPort.findById(doctorId)
                .orElseThrow(() -> AppointmentSchedulingException.doctorNotFound(doctorId));

        if (!doctorSchedulePort.isDateWithinBookingWindow(doctorId, date)) {
            return AvailableSlotsResultDTO.builder()
                    .doctorId(doctorId)
                    .doctorName(doctor.fullName())
                    .date(date)
                    .total(0)
                    .slots(List.of())
                    .message("La fecha esta fuera de la ventana de semanas configurada para agendar.")
                    .build();
        }

        Set<LocalDateTime> occupied = appointmentRepository
                .findByDoctorAndDateRange(doctorId, date.atStartOfDay(), date.atTime(23, 59, 59))
                .stream()
                .filter(appointment -> OCCUPYING_STATUSES.contains(appointment.getStatus()))
                .map(Appointment::getAppointmentDate)
                .collect(Collectors.toSet());

        List<TimeSlotDTO> available = doctorSchedulePort.candidateSlots(doctorId, date).stream()
                .filter(slot -> !occupied.contains(slot))
                .map(slot -> TimeSlotDTO.builder().start(slot).build())
                .toList();

        String message = available.isEmpty()
                ? "No existen franjas disponibles para el profesional en la fecha seleccionada."
                : null;

        return AvailableSlotsResultDTO.builder()
                .doctorId(doctorId)
                .doctorName(doctor.fullName())
                .date(date)
                .total(available.size())
                .slots(available)
                .message(message)
                .build();
    }
}
