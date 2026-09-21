package com.groupsoft.piedrazul.availability.application.mapper;

import com.groupsoft.piedrazul.availability.application.dto.DoctorSchedulingConfigDTO;
import com.groupsoft.piedrazul.availability.application.dto.TimeRangeDTO;
import com.groupsoft.piedrazul.availability.domain.model.DoctorSchedulingConfig;
import com.groupsoft.piedrazul.availability.domain.model.TimeRange;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DoctorSchedulingConfigMapper {

    public DoctorSchedulingConfigDTO toDto(DoctorSchedulingConfig config) {
        List<TimeRangeDTO> ranges = config.getTimeSlots().stream()
                .map(range -> TimeRangeDTO.builder()
                        .startTime(range.getStartTime())
                        .endTime(range.getEndTime())
                        .build())
                .toList();

        return DoctorSchedulingConfigDTO.builder()
                .doctorId(config.getDoctorId())
                .bookingWindowWeeks(config.getBookingWindowWeeks())
                .workingDays(config.getWorkingDays())
                .timeSlots(ranges)
                .slotIntervalMinutes(config.getSlotIntervalMinutes())
                .build();
    }

    public List<TimeRange> toTimeRanges(List<TimeRangeDTO> slots) {
        return slots.stream()
                .map(slot -> new TimeRange(slot.getStartTime(), slot.getEndTime()))
                .toList();
    }
}
