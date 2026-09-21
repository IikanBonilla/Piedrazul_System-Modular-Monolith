package com.groupsoft.piedrazul.availability.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class DoctorSchedulingConfigDTO {
    private Long doctorId;
    private Integer bookingWindowWeeks;
    private Set<DayOfWeek> workingDays;
    private List<TimeRangeDTO> timeSlots;
    private Integer slotIntervalMinutes;
    private String message;
}
