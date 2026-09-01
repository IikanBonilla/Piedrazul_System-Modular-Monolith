package com.groupsoft.piedrazul.appointment.infrastructure.persistence;

import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentJpaRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorIdAndAppointmentDateBetween(
            Long doctorId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
