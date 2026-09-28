package com.groupsoft.piedrazul.user.application.usecase;

import com.groupsoft.piedrazul.user.application.dto.LoginRequest;
import com.groupsoft.piedrazul.user.application.exception.AuthException;
import com.groupsoft.piedrazul.user.domain.model.Role;
import com.groupsoft.piedrazul.user.domain.model.User;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(userRepository, passwordEncoder);
    }

    @Test
    void logsInWithTheRightPassword() {
        User user = user("paciente", passwordEncoder.encode("paciente123"), true);
        when(userRepository.findByUsernameIgnoreCase("paciente")).thenReturn(Optional.of(user));

        var result = useCase.execute(new LoginRequest("Paciente", "paciente123"));

        assertEquals("paciente", result.username());
        assertEquals("PATIENT", result.role());
    }

    @Test
    void rejectsAWrongPassword() {
        User user = user("paciente", passwordEncoder.encode("paciente123"), true);
        when(userRepository.findByUsernameIgnoreCase("paciente")).thenReturn(Optional.of(user));

        AuthException error = assertThrows(
                AuthException.class,
                () -> useCase.execute(new LoginRequest("paciente", "otra-clave"))
        );
        assertEquals("INVALID_CREDENTIALS", error.getCode());
    }

    @Test
    void rejectsAnUnknownUser() {
        when(userRepository.findByUsernameIgnoreCase("nadie")).thenReturn(Optional.empty());

        AuthException error = assertThrows(
                AuthException.class,
                () -> useCase.execute(new LoginRequest("nadie", "paciente123"))
        );
        assertEquals("INVALID_CREDENTIALS", error.getCode());
    }

    @Test
    void rejectsEmptyFields() {
        AuthException error = assertThrows(
                AuthException.class,
                () -> useCase.execute(new LoginRequest("  ", ""))
        );
        assertEquals("VALIDATION_ERROR", error.getCode());
        assertEquals("Este campo es obligatorio.", error.getFields().get("username"));
        assertEquals("Este campo es obligatorio.", error.getFields().get("password"));
    }

    @Test
    void upgradesALegacyPlainTextPassword() {
        User user = user("paciente", "paciente123", true);
        when(userRepository.findByUsernameIgnoreCase("paciente")).thenReturn(Optional.of(user));

        useCase.execute(new LoginRequest("paciente", "paciente123"));

        assertTrue(user.getPassword().startsWith("$2"));
        verify(userRepository).save(user);
    }

    private User user(String username, String password, boolean active) {
        return User.builder()
                .id(1L)
                .username(username)
                .password(password)
                .fullName("Juan Perez")
                .role(Role.PATIENT)
                .active(active)
                .build();
    }
}
