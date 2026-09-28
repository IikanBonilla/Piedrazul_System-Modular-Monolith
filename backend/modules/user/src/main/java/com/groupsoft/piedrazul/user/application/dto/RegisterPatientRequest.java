package com.groupsoft.piedrazul.user.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Entrada HE-02 del registro de paciente. Los datos se validan y persisten
 * con el registro unificado ({@code RegisterUserUseCase} / {@code RegistrationRules}).
 */
@Data
@Builder
public class RegisterPatientRequest {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String documentNumber;
    private String phone;
    private LocalDate birthDate;
}
