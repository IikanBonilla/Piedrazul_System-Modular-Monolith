package com.groupsoft.piedrazul.availability.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DoctorWorkingHoursTest {

    private final DoctorWorkingHours workingHours = new DoctorWorkingHours();

    @Test
    void shouldGenerateDefaultWeekdaySlots() {
        LocalDate monday = LocalDate.of(2026, 9, 21);
        List<LocalDateTime> slots = workingHours.slotsFor(monday, DoctorSchedulingConfig.defaultFor(1L));

        assertEquals(16, slots.size());
        assertTrue(slots.contains(monday.atTime(9, 0)));
        assertTrue(slots.contains(monday.atTime(14, 0)));
        assertFalse(slots.contains(monday.atTime(12, 0)));
    }

    @Test
    void shouldRespectCustomIntervalAndRanges() {
        LocalDate monday = LocalDate.of(2026, 9, 21);
        DoctorSchedulingConfig config = DoctorSchedulingConfig.builder()
                .doctorId(1L)
                .bookingWindowWeeks(2)
                .slotIntervalMinutes(60)
                .workingDays(EnumSet.of(DayOfWeek.MONDAY))
                .timeSlots(List.of(new TimeRange(LocalTime.of(8, 0), LocalTime.of(10, 0))))
                .build();

        List<LocalDateTime> slots = workingHours.slotsFor(monday, config);
        assertEquals(List.of(monday.atTime(8, 0), monday.atTime(9, 0)), slots);
    }

    @Test
    void shouldReturnEmptyWhenDayIsNotConfigured() {
        LocalDate sunday = LocalDate.of(2026, 9, 20);
        assertTrue(workingHours.slotsFor(sunday, DoctorSchedulingConfig.defaultFor(1L)).isEmpty());
    }
}
