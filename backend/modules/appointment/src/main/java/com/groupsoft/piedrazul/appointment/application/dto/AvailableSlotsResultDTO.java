package com.groupsoft.piedrazul.appointment.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AvailableSlotsResultDTO {
    private Long doctorId;
    private String doctorName;
    private LocalDate date;
    private int total;
    private List<TimeSlotDTO> slots;
    private String message;
}
