package com.groupsoft.piedrazul.user.application.validation;

import com.groupsoft.piedrazul.user.application.dto.RegisterUserRequest;
import com.groupsoft.piedrazul.user.domain.model.Role;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class RegistrationRules {

    private static final Pattern NAME = Pattern.compile(
            "^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(?:[ -][A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$"
    );
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^3\\d{9}$");
    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9._]{4,30}$");
    private static final Pattern DOCUMENT = Pattern.compile("^\\d{6,15}$");

    private RegistrationRules() {
    }

    public static Map<String, String> validate(RegisterUserRequest request, LocalDate today) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (request == null) {
            errors.put("form", "Debes enviar los datos del registro.");
            return errors;
        }

        put(errors, "fullName", personName(request.fullName()));
        put(errors, "username", username(request.username()));
        put(errors, "email", email(request.email()));
        put(errors, "password", password(request.password()));
        put(errors, "phone", phone(request.phone()));
        put(errors, "documentNumber", documentNumber(request.documentNumber()));
        put(errors, "birthDate", birthDate(request.birthDate(), today));
        put(errors, "role", role(request.role()));

        if ("DOCTOR".equals(normalizeRole(request.role()))) {
            put(errors, "specialty", specialty(request.specialty()));
        }
        return errors;
    }

    public static String personName(String value) {
        String normalized = normalizeName(value);
        if (normalized.isEmpty()) {
            return "Este campo es obligatorio.";
        }
        if (normalized.length() < 2 || normalized.length() > 80) {
            return "El nombre debe tener entre 2 y 80 caracteres.";
        }
        if (!NAME.matcher(normalized).matches()) {
            return "Usa solo letras. Se permiten espacios, tildes y la eñe.";
        }
        return null;
    }

    public static String username(String value) {
        String normalized = normalizeUsername(value);
        if (normalized.isEmpty()) {
            return "Este campo es obligatorio.";
        }
        if (!USERNAME.matcher(normalized).matches()) {
            return "El usuario debe tener entre 4 y 30 caracteres: letras, números, punto o guion bajo.";
        }
        return null;
    }

    public static String email(String value) {
        String normalized = normalizeEmail(value);
        if (normalized.isEmpty()) {
            return "Este campo es obligatorio.";
        }
        if (normalized.length() > 120 || normalized.contains(" ") || !EMAIL.matcher(normalized).matches()) {
            return "Ingresa un correo electrónico válido.";
        }
        return null;
    }

    public static String password(String value) {
        if (value == null || value.isBlank()) {
            return "Este campo es obligatorio.";
        }
        if (value.length() < 8 || value.length() > 64) {
            return "La contraseña debe tener entre 8 y 64 caracteres.";
        }
        boolean hasLetter = value.chars().anyMatch(Character::isLetter);
        boolean hasDigit = value.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            return "La contraseña debe incluir al menos una letra y un número.";
        }
        return null;
    }

    public static String phone(String value) {
        if (value == null || value.isBlank()) {
            return "Este campo es obligatorio.";
        }
        if (!value.equals(value.trim()) || value.contains(" ")) {
            return "El número de teléfono no debe contener espacios.";
        }
        if (!PHONE.matcher(value).matches()) {
            return "El número de teléfono debe tener 10 dígitos y comenzar por 3.";
        }
        return null;
    }

    public static String documentNumber(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            return "Este campo es obligatorio.";
        }
        if (!DOCUMENT.matcher(normalized).matches()) {
            return "El documento debe tener entre 6 y 15 dígitos, sin letras ni espacios.";
        }
        return null;
    }

    public static String birthDate(LocalDate birthDate, LocalDate today) {
        if (birthDate == null) {
            return "Este campo es obligatorio.";
        }
        if (birthDate.isAfter(today)) {
            return "La fecha de nacimiento no puede ser posterior a hoy.";
        }
        if (birthDate.isBefore(today.minusYears(120))) {
            return "La fecha de nacimiento no es válida.";
        }
        return null;
    }

    public static String birthDate(String raw, LocalDate today) {
        if (raw == null || raw.isBlank()) {
            return "Este campo es obligatorio.";
        }
        try {
            return birthDate(LocalDate.parse(raw.trim()), today);
        } catch (DateTimeParseException ex) {
            return "Ingresa una fecha de nacimiento válida.";
        }
    }

    public static String role(String value) {
        String normalized = normalizeRole(value);
        if (normalized.isEmpty()) {
            return "Selecciona un rol.";
        }
        if ("ADMINISTRATOR".equals(normalized) || "SCHEDULER".equals(normalized)) {
            return "El registro de administradores no está disponible en el formulario público.";
        }
        if (!"PATIENT".equals(normalized) && !"DOCTOR".equals(normalized)) {
            return "Selecciona un rol válido: paciente o médico.";
        }
        return null;
    }

    public static String specialty(String value) {
        String normalized = normalizeName(value);
        if (normalized.isEmpty()) {
            return "Indica la especialidad del médico.";
        }
        if (normalized.length() < 3 || normalized.length() > 60 || !NAME.matcher(normalized).matches()) {
            return "La especialidad solo puede contener letras y debe tener entre 3 y 60 caracteres.";
        }
        return null;
    }

    public static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    public static String normalizeUsername(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeEmail(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeRole(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public static Role toPublicRole(String value) {
        return Role.valueOf(normalizeRole(value));
    }

    private static void put(Map<String, String> errors, String field, String message) {
        if (message != null) {
            errors.put(field, message);
        }
    }
}
