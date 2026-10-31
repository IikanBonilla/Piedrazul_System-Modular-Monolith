package com.groupsoft.piedrazul.availability.infrastructure.adapter;

import com.groupsoft.piedrazul.availability.domain.model.DoctorSchedulingConfig;
import com.groupsoft.piedrazul.availability.domain.model.DoctorWorkingHours;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorSchedulingConfigRepository;
import com.groupsoft.piedrazul.shared.port.DoctorSchedulePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DoctorScheduleAdapter implements DoctorSchedulePort {

    private final DoctorSchedulingConfigRepository configRepository;
    private final DoctorWorkingHours workingHours = new DoctorWorkingHours();

    @Override
    public List<LocalDateTime> candidateSlots(Long doctorId, LocalDate date) {
        return workingHours.slotsFor(date, loadOrDefault(doctorId));
    }

    @Override
    public boolean isDateWithinBookingWindow(Long doctorId, LocalDate date) {
        return workingHours.isWithinBookingWindow(date, loadOrDefault(doctorId), LocalDate.now());
    }

    private DoctorSchedulingConfig loadOrDefault(Long doctorId) {
        return configRepository.findByDoctorId(doctorId)
                .orElseGet(() -> DoctorSchedulingConfig.defaultFor(doctorId));
    }
}
