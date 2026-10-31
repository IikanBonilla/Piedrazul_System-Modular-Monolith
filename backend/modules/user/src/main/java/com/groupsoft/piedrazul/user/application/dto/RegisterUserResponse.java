package com.groupsoft.piedrazul.user.application.dto;

public record RegisterUserResponse(
        Long id,
        String username,
        String role,
        String message
) {
}
