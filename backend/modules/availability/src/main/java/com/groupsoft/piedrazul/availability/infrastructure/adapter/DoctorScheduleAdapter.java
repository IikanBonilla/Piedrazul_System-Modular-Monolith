package com.groupsoft.piedrazul.availability.infrastructure.adapter;

import com.groupsoft.piedrazul.availability.domain.model.DoctorWorkingHours;
import com.groupsoft.piedrazul.shared.port.DoctorSchedulePort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DoctorScheduleAdapter implements DoctorSchedulePort {

    private final DoctorWorkingHours workingHours = new DoctorWorkingHours();

    @Override
    public List<LocalDateTime> candidateSlots(LocalDate date) {
        return workingHours.slotsFor(date);
    }
}
