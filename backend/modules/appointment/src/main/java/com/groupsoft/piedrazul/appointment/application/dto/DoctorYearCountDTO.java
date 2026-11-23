package com.groupsoft.piedrazul.appointment.application.dto;

import java.util.List;

public record DoctorYearCountDTO(
        Long doctorId,
        String doctorName,
        List<Integer> counts,
        int total
) {
}
