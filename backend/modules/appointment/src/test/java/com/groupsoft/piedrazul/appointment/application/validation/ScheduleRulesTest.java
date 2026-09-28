package com.groupsoft.piedrazul.appointment.application.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScheduleRulesTest {

    @Test
    void acceptsARangeThatEndsLater() {
        assertNull(ScheduleRules.timeRangeError("08:00", "12:00"));
    }

    @Test
    void rejectsARangeThatEndsEarlier() {
        assertEquals(
                "La hora de fin debe ser posterior a la hora de inicio.",
                ScheduleRules.timeRangeError("12:00", "08:00")
        );
    }

    @Test
    void rejectsAnEqualRange() {
        assertEquals(
                "La hora de fin debe ser posterior a la hora de inicio.",
                ScheduleRules.timeRangeError("10:00", "10:00")
        );
    }
}
