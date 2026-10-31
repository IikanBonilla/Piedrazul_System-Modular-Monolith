package com.groupsoft.piedrazul.availability.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class TimeRangeDTO {
    private LocalTime startTime;
    private LocalTime endTime;
}
