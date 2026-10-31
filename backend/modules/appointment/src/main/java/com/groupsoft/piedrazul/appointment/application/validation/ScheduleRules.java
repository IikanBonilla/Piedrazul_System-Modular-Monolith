package com.groupsoft.piedrazul.appointment.application.validation;

import java.util.regex.Pattern;

public final class ScheduleRules {

    private static final Pattern TIME = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    private ScheduleRules() {
    }

    public static String timeRangeError(String start, String end) {
        if (start == null || start.isBlank() || end == null || end.isBlank()) {
            return "Indica la hora de inicio y la hora de fin.";
        }
        if (!TIME.matcher(start).matches() || !TIME.matcher(end).matches()) {
            return "Ingresa un horario válido en formato HH:mm.";
        }
        if (start.compareTo(end) >= 0) {
            return "La hora de fin debe ser posterior a la hora de inicio.";
        }
        return null;
    }
}
