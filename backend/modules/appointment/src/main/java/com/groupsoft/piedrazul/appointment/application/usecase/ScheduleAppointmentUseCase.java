package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.ScheduleAppointmentRequest;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSchedulingException;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import com.groupsoft.piedrazul.shared.port.DoctorQueryPort;
import com.groupsoft.piedrazul.shared.port.DoctorSchedulePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HE-02 / HE-03: solo permite agendar franjas generadas por la config del medico.
 */
@Service
@RequiredArgsConstructor
public class ScheduleAppointmentUseCase {

    private final AppointmentRepositoryPort appointmentRepository;
    private final DoctorQueryPort doctorQueryPort;
    private final DoctorSchedulePort doctorSchedulePort;
    private final QueryAvailableSlotsUseCase queryAvailableSlotsUseCase;

    @Transactional
    public Appointment execute(ScheduleAppointmentRequest request) {
        ScheduleAppointmentRequest payload = request == null
                ? ScheduleAppointmentRequest.builder().build()
                : request;

        if (payload.getDoctorId() == null) {
            throw AppointmentSchedulingException.missingDoctor();
        }
        if (payload.getSlot() == null) {
            throw AppointmentSchedulingException.missingSlot();
        }

        doctorQueryPort.findById(payload.getDoctorId())
                .orElseThrow(() -> AppointmentSchedulingException.doctorNotFound(payload.getDoctorId()));

        LocalDateTime slot = payload.getSlot();
        List<LocalDateTime> available = queryAvailableSlotsUseCase
                .execute(payload.getDoctorId(), slot.toLocalDate())
                .getSlots()
                .stream()
                .map(item -> item.getStart())
                .toList();

        if (available.isEmpty() && !doctorSchedulePort.isDateWithinBookingWindow(
                payload.getDoctorId(), slot.toLocalDate())) {
            throw AppointmentSchedulingException.slotNotAvailable();
        }
        if (!available.contains(slot)) {
            throw AppointmentSchedulingException.slotNotAvailable();
        }

        return appointmentRepository.save(Appointment.builder()
                .patientId(payload.getPatientId())
                .doctorId(payload.getDoctorId())
                .appointmentDate(slot)
                .status(AppointmentStatus.CONFIRMED)
                .build());
    }
}
