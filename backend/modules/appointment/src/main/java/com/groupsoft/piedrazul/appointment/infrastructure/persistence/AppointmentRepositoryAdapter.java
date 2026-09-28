package com.groupsoft.piedrazul.appointment.infrastructure.persistence;

import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
    public boolean existsActiveByDoctorAndDateTime(Long doctorId, LocalDateTime appointmentDate) {
        return jpaRepository.existsByDoctorIdAndAppointmentDateAndStatusIn(
                doctorId,
                appointmentDate,
                Set.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED)
        );
    }

    @Override
    public Appointment save(Appointment appointment) {
        return jpaRepository.save(appointment);
    }
}
