package com.groupsoft.piedrazul.user.application.usecase;

import com.groupsoft.piedrazul.user.application.dto.AuthenticatedUser;
import com.groupsoft.piedrazul.user.application.dto.LoginRequest;
import com.groupsoft.piedrazul.user.application.exception.AuthException;
import com.groupsoft.piedrazul.user.application.validation.RegistrationRules;
import com.groupsoft.piedrazul.user.domain.model.User;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthenticatedUser execute(LoginRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        String username = request == null ? "" : RegistrationRules.normalizeUsername(request.username());
        String password = request == null ? null : request.password();

        if (username.isBlank()) {
            errors.put("username", "Este campo es obligatorio.");
        }
        if (password == null || password.isBlank()) {
            errors.put("password", "Este campo es obligatorio.");
        }
        if (!errors.isEmpty()) {
            throw AuthException.validation(errors);
        }

        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(AuthException::invalidCredentials);

        if (!passwordMatches(user, password)) {
            throw AuthException.invalidCredentials();
        }
        if (!user.isActive()) {
            throw AuthException.inactive();
        }

        return new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name(),
                user.getDoctorId(),
                user.getEmail(),
                user.getPhone()
        );
    }

    private boolean passwordMatches(User user, String rawPassword) {
        String stored = user.getPassword();
        if (isBcrypt(stored)) {
            return passwordEncoder.matches(rawPassword, stored);
        }
        if (stored == null || !stored.equals(rawPassword)) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        return true;
    }

    private boolean isBcrypt(String value) {
        return value != null
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
