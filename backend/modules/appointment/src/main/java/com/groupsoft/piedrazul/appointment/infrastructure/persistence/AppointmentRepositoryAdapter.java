package com.groupsoft.piedrazul.appointment.infrastructure.persistence;

import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AppointmentRepositoryAdapter implements AppointmentRepositoryPort {

    private final AppointmentJpaRepository jpaRepository;

    @Override
    public List<Appointment> findByDoctorAndDateRange(
            Long doctorId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        return jpaRepository.findByDoctorIdAndAppointmentDateBetween(
                doctorId, startOfDay, endOfDay);
    }

    @Override
    public Appointment save(Appointment appointment) {
        return jpaRepository.save(appointment);
    }
}
