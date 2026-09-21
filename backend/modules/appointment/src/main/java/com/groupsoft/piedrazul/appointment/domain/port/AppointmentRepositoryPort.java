package com.groupsoft.piedrazul.appointment.domain.port;

import com.groupsoft.piedrazul.appointment.domain.model.Appointment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Puerto de salida del dominio (DIP): el caso de uso no conoce JPA.
 */
public interface AppointmentRepositoryPort {

    List<Appointment> findByDoctorAndDateRange(
            Long doctorId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    boolean existsActiveByDoctorAndDateTime(Long doctorId, LocalDateTime appointmentDate);

    Appointment save(Appointment appointment);
}
