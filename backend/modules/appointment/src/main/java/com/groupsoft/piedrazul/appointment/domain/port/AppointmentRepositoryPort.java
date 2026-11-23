package com.groupsoft.piedrazul.appointment.domain.port;

import com.groupsoft.piedrazul.appointment.domain.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del dominio (DIP): el caso de uso no conoce JPA.
 */
public interface AppointmentRepositoryPort {

    List<Appointment> findByDoctorAndDateRange(
            Long doctorId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Appointment> findByPatientId(Long patientId);

    Optional<Appointment> findById(Long appointmentId);

    List<Appointment> findByDateRange(LocalDateTime startInclusive, LocalDateTime endExclusive);

    boolean existsActiveByDoctorAndDateTime(Long doctorId, LocalDateTime appointmentDate);

    Appointment save(Appointment appointment);
}
