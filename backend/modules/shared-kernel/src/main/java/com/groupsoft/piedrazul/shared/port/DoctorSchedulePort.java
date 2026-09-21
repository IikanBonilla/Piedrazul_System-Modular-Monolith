package com.groupsoft.piedrazul.shared.port;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ISP: el modulo de citas consulta las franjas candidatas del horario clinico,
 * sin conocer como se generan.
 */
public interface DoctorSchedulePort {

    List<LocalDateTime> candidateSlots(LocalDate date);
}
