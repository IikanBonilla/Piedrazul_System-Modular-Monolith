package com.groupsoft.piedrazul.appointment.application.dto;

import java.util.List;

public record AppointmentYearReportDTO(
        int year,
        int total,
        List<String> months,
        List<DoctorYearCountDTO> doctors
) {
}
