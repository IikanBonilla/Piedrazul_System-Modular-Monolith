package com.groupsoft.piedrazul.shared.port;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ISP: el modulo de citas consulta franjas candidatas segun la config del medico.
 */
public interface DoctorSchedulePort {

    List<LocalDateTime> candidateSlots(Long doctorId, LocalDate date);

    boolean isDateWithinBookingWindow(Long doctorId, LocalDate date);
}
