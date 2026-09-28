package com.groupsoft.piedrazul.user.application.validation;

import com.groupsoft.piedrazul.user.application.dto.RegisterUserRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationRulesTest {

    private final LocalDate today = LocalDate.of(2026, 9, 26);

    @Test
    void acceptsAValidPatient() {
        Map<String, String> errors = RegistrationRules.validate(validPatient(today), today);
        assertTrue(errors.isEmpty());
    }

    @Test
    void rejectsEmptyFields() {
        Map<String, String> errors = RegistrationRules.validate(
                new RegisterUserRequest(" ", "", "", "", "", "", null, "", ""),
                today
        );
        assertEquals("Este campo es obligatorio.", errors.get("fullName"));
        assertEquals("Este campo es obligatorio.", errors.get("username"));
        assertEquals("Este campo es obligatorio.", errors.get("email"));
        assertEquals("Este campo es obligatorio.", errors.get("password"));
        assertEquals("Este campo es obligatorio.", errors.get("phone"));
        assertEquals("Este campo es obligatorio.", errors.get("birthDate"));
    }

    @Test
    void rejectsInvalidEmail() {
        assertEquals("Ingresa un correo electrónico válido.", RegistrationRules.email("usuario@"));
        assertNull(RegistrationRules.email("usuario@gmail.com"));
    }

    @Test
    void validatesPhoneNumbers() {
        assertNull(RegistrationRules.phone("3001234567"));
        assertEquals(
                "El número de teléfono debe tener 10 dígitos y comenzar por 3.",
                RegistrationRules.phone("2001234567")
        );
        assertEquals(
                "El número de teléfono debe tener 10 dígitos y comenzar por 3.",
                RegistrationRules.phone("30012345")
        );
        assertEquals(
                "El número de teléfono debe tener 10 dígitos y comenzar por 3.",
                RegistrationRules.phone("30012345678")
        );
    }

    @Test
    void validatesPersonNames() {
        assertNull(RegistrationRules.personName("Juan Carlos"));
        assertNull(RegistrationRules.personName("Ángel Núñez"));
        assertEquals(
                "Usa solo letras. Se permiten espacios, tildes y la eñe.",
                RegistrationRules.personName("Juan123")
        );
    }

    @Test
    void validatesBirthDate() {
        assertNull(RegistrationRules.birthDate(today, today));
        assertNull(RegistrationRules.birthDate(today.minusYears(30), today));
        assertEquals(
                "La fecha de nacimiento no puede ser posterior a hoy.",
                RegistrationRules.birthDate(today.plusDays(1), today)
        );
    }

    @Test
    void rejectsPublicAdministratorRegistration() {
        RegisterUserRequest request = new RegisterUserRequest(
                "Ana Gómez",
                "anagomez",
                "ana@gmail.com",
                "clave1234",
                "3001234567",
                "12345678",
                today.minusYears(40),
                "ADMINISTRATOR",
                ""
        );
        assertEquals(
                "El registro de administradores no está disponible en el formulario público.",
                RegistrationRules.validate(request, today).get("role")
        );
    }

    private RegisterUserRequest validPatient(LocalDate birthDate) {
        return new RegisterUserRequest(
                "Juan Carlos",
                "juancarlos",
                "usuario@gmail.com",
                "clave1234",
                "3001234567",
                "12345678",
                birthDate.minusYears(20),
                "PATIENT",
                ""
        );
    }
}
