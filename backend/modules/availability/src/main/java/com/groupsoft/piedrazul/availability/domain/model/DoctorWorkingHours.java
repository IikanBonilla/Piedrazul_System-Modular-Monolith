package com.groupsoft.piedrazul.availability.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Horario clinico de atencion. Domingo no hay atencion.
 * Franjas de 30 minutos: 08:00-12:00 y 14:00-18:00.
 */
public final class DoctorWorkingHours {

    public static final int SLOT_MINUTES = 30;

    public List<LocalDateTime> slotsFor(LocalDate date) {
        if (date == null || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of();
        }

        List<LocalDateTime> slots = new ArrayList<>();
        addRange(slots, date, LocalTime.of(8, 0), LocalTime.of(12, 0));
        addRange(slots, date, LocalTime.of(14, 0), LocalTime.of(18, 0));
        return List.copyOf(slots);
    }

    private void addRange(List<LocalDateTime> slots, LocalDate date, LocalTime start, LocalTime end) {
        LocalDateTime current = date.atTime(start);
        LocalDateTime limit = date.atTime(end);
        while (current.isBefore(limit)) {
            slots.add(current);
            current = current.plusMinutes(SLOT_MINUTES);
        }
    }
}
