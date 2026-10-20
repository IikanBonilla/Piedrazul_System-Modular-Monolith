package com.groupsoft.piedrazul.user.application.dto;

public record SessionProfile(
        long expiresAt,
        Long id,
        String username,
        String fullName,
        String role,
        Long doctorId,
        String email,
        String phone
) {
}
