package com.groupsoft.piedrazul.appointment.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TimeSlotDTO {
    private LocalDateTime start;
}
