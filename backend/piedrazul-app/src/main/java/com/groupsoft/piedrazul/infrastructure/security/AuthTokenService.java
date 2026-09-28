package com.groupsoft.piedrazul.infrastructure.security;

import com.groupsoft.piedrazul.user.application.dto.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {

    private static final Duration TTL = Duration.ofHours(8);

    private final ConcurrentHashMap<String, IssuedToken> tokens = new ConcurrentHashMap<>();

    public IssuedToken issue(AuthenticatedUser user) {
        String token = UUID.randomUUID().toString();
        IssuedToken issued = new IssuedToken(
                token,
                Instant.now().plus(TTL).toEpochMilli(),
                user.id(),
                user.role()
        );
        tokens.put(token, issued);
        return issued;
    }

    public Optional<IssuedToken> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        IssuedToken issued = tokens.get(token);
        if (issued == null) {
            return Optional.empty();
        }
        if (issued.expiresAt() <= Instant.now().toEpochMilli()) {
            tokens.remove(token);
            return Optional.empty();
        }
        return Optional.of(issued);
    }
}
