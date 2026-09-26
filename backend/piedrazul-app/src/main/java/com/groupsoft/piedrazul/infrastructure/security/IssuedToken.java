package com.groupsoft.piedrazul.infrastructure.security;

public record IssuedToken(
        String token,
        long expiresAt,
        Long userId,
        String role
) {
}
