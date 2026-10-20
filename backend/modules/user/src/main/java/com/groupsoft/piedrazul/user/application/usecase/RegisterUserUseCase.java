package com.groupsoft.piedrazul.user.application.usecase;

import com.groupsoft.piedrazul.user.application.dto.RegisterUserRequest;
import com.groupsoft.piedrazul.user.application.exception.RegistrationException;
import com.groupsoft.piedrazul.user.application.port.IdentityProvisioner;
import com.groupsoft.piedrazul.user.application.validation.RegistrationRules;
import com.groupsoft.piedrazul.user.domain.model.User;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdentityProvisioner identityProvisioner;

    @Transactional
    public User execute(RegisterUserRequest request) {
        RegisterUserRequest normalized = normalize(request);
        Map<String, String> errors = RegistrationRules.validate(normalized, LocalDate.now());
        if (!errors.isEmpty()) {
            throw RegistrationException.validation(errors);
        }

        if (userRepository.findByUsernameIgnoreCase(normalized.username()).isPresent()) {
            throw RegistrationException.conflict("username", "Este usuario ya está registrado.");
        }
        if (userRepository.findByEmailIgnoreCase(normalized.email()).isPresent()) {
            throw RegistrationException.conflict("email", "Este correo electrónico ya está registrado.");
        }
        if (userRepository.findByDocumentNumber(normalized.documentNumber()).isPresent()) {
            throw RegistrationException.conflict("documentNumber", "Este documento ya está registrado.");
        }

        String role = RegistrationRules.toPublicRole(normalized.role()).name();
        identityProvisioner.provision(
                normalized.username(),
                normalized.email(),
                normalized.fullName(),
                normalized.password(),
                role
        );

        User user = User.builder()
                .username(normalized.username())
                .password(passwordEncoder.encode(normalized.password()))
                .fullName(normalized.fullName())
                .email(normalized.email())
                .documentNumber(normalized.documentNumber())
                .phone(normalized.phone())
                .birthDate(normalized.birthDate())
                .role(RegistrationRules.toPublicRole(normalized.role()))
                .active(true)
                .build();

        try {
            return userRepository.save(user);
        } catch (RuntimeException ex) {
            identityProvisioner.rollback(normalized.username());
            throw ex;
        }
    }

    private RegisterUserRequest normalize(RegisterUserRequest request) {
        if (request == null) {
            return null;
        }
        return new RegisterUserRequest(
                RegistrationRules.normalizeName(request.fullName()),
                RegistrationRules.normalizeUsername(request.username()),
                RegistrationRules.normalizeEmail(request.email()),
                request.password(),
                request.phone() == null ? "" : request.phone().trim(),
                request.documentNumber() == null ? "" : request.documentNumber().trim(),
                request.birthDate(),
                RegistrationRules.normalizeRole(request.role()),
                RegistrationRules.normalizeName(request.specialty())
        );
    }
}
