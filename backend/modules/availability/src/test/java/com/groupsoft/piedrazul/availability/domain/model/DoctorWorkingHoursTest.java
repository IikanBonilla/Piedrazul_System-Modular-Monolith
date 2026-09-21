package com.groupsoft.piedrazul.availability.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DoctorWorkingHoursTest {

    private final DoctorWorkingHours workingHours = new DoctorWorkingHours();

    @Test
    void shouldGenerateWeekdaySlotsInWorkingHours() {
        LocalDate monday = LocalDate.of(2026, 9, 21);
        List<LocalDateTime> slots = workingHours.slotsFor(monday);

        assertEquals(16, slots.size());
        assertTrue(slots.contains(monday.atTime(8, 0)));
        assertTrue(slots.contains(monday.atTime(9, 0)));
        assertTrue(slots.contains(monday.atTime(10, 30)));
        assertTrue(slots.contains(monday.atTime(17, 30)));
        assertFalse(slots.contains(monday.atTime(12, 0)));
        assertFalse(slots.contains(monday.atTime(13, 0)));
    }

    @Test
    void shouldReturnNoSlotsOnSunday() {
        assertTrue(workingHours.slotsFor(LocalDate.of(2026, 9, 20)).isEmpty());
    }
}
