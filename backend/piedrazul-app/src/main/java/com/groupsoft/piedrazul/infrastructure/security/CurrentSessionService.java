package com.groupsoft.piedrazul.infrastructure.security;

import com.groupsoft.piedrazul.user.application.dto.SessionProfile;
import com.groupsoft.piedrazul.user.application.exception.AuthException;
import com.groupsoft.piedrazul.user.domain.model.User;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentSessionService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SessionProfile profile(Jwt jwt) {
        if (jwt == null) {
            throw new AuthException("UNAUTHORIZED", "Debes iniciar sesión.");
        }
        String role = KeycloakRealmRoleConverter.primaryApplicationRole(jwt);
        if (role == null) {
            throw new AuthException("FORBIDDEN", "No tienes permiso para acceder a esta función.");
        }
        String username = KeycloakRealmRoleConverter.username(jwt);
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(AuthException::notLinked);
        if (!user.isActive()) {
            throw AuthException.inactive();
        }
        long expiresAt = jwt.getExpiresAt() == null ? 0L : jwt.getExpiresAt().toEpochMilli();
        return new SessionProfile(
                expiresAt,
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                role,
                user.getDoctorId(),
                user.getEmail(),
                user.getPhone()
        );
    }
}
