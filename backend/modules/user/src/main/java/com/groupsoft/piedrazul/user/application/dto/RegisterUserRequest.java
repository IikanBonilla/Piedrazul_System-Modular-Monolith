package com.groupsoft.piedrazul.user.application.dto;

import java.time.LocalDate;

public record RegisterUserRequest(
        String fullName,
        String username,
        String email,
        String password,
        String phone,
        String documentNumber,
        LocalDate birthDate,
        String role,
        String specialty
) {
}
