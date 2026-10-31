package com.groupsoft.piedrazul.availability.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera franjas a partir de la configuracion del medico (HE-03).
 * Default historico: lun-sab, 08:00-12:00 y 14:00-18:00, 30 min.
 */
public final class DoctorWorkingHours {

    public List<LocalDateTime> slotsFor(LocalDate date, DoctorSchedulingConfig config) {
        if (date == null || config == null) {
            return List.of();
        }
        if (config.getWorkingDays() == null || !config.getWorkingDays().contains(date.getDayOfWeek())) {
            return List.of();
        }

        int interval = config.getSlotIntervalMinutes();
        if (interval <= 0) {
            return List.of();
        }

        List<LocalDateTime> slots = new ArrayList<>();
        List<TimeRange> ranges = config.getTimeSlots() == null ? List.of() : config.getTimeSlots();
        for (TimeRange range : ranges) {
            addRange(slots, date, range.getStartTime(), range.getEndTime(), interval);
        }
        return List.copyOf(slots);
    }

    public boolean isWithinBookingWindow(LocalDate date, DoctorSchedulingConfig config, LocalDate today) {
        if (date == null || config == null || today == null) {
            return false;
        }
        if (date.isBefore(today)) {
            return false;
        }
        LocalDate lastDay = today.plusWeeks(config.getBookingWindowWeeks());
        return !date.isAfter(lastDay);
    }

    private void addRange(
            List<LocalDateTime> slots,
            LocalDate date,
            LocalTime start,
            LocalTime end,
            int intervalMinutes) {
        if (start == null || end == null || !start.isBefore(end)) {
            return;
        }
        LocalDateTime current = date.atTime(start);
        LocalDateTime limit = date.atTime(end);
        while (current.isBefore(limit)) {
            slots.add(current);
            current = current.plusMinutes(intervalMinutes);
        }
    }
}
