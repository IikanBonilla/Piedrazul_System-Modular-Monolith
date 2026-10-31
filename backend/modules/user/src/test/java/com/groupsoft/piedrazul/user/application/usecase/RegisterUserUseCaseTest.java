package com.groupsoft.piedrazul.user.application.usecase;

import com.groupsoft.piedrazul.user.application.dto.RegisterUserRequest;
import com.groupsoft.piedrazul.user.application.port.IdentityProvisioner;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IdentityProvisioner identityProvisioner;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterUserUseCase(userRepository, passwordEncoder, identityProvisioner);
    }

    @Test
    void createsTheIdentityBeforeSavingTheLocalProfile() {
        when(userRepository.findByUsernameIgnoreCase("juancarlos")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("usuario@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByDocumentNumber("12345678")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = useCase.execute(patient());

        verify(identityProvisioner).provision(
                "juancarlos",
                "usuario@gmail.com",
                "Juan Carlos",
                "clave1234",
                "PATIENT"
        );
        assertEquals(Role.PATIENT, saved.getRole());
        assertEquals("juancarlos", saved.getUsername());
    }

    @Test
    void rollsBackTheIdentityWhenTheLocalSaveFails() {
        when(userRepository.findByUsernameIgnoreCase("juancarlos")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("usuario@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByDocumentNumber("12345678")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenThrow(new IllegalStateException("db"));

        try {
            useCase.execute(patient());
        } catch (IllegalStateException ignored) {
            verify(identityProvisioner).rollback("juancarlos");
            return;
        }
        throw new AssertionError("debio fallar el guardado local");
    }

    @Test
    void doesNotProvisionWhenTheUsernameAlreadyExists() {
        when(userRepository.findByUsernameIgnoreCase("juancarlos")).thenReturn(Optional.of(User.builder().build()));

        try {
            useCase.execute(patient());
        } catch (RuntimeException ignored) {
            verify(identityProvisioner, never()).provision(any(), any(), any(), any(), any());
            return;
        }
        throw new AssertionError("debio rechazar el usuario duplicado");
    }

    private RegisterUserRequest patient() {
        return new RegisterUserRequest(
                "Juan Carlos",
                "juancarlos",
                "usuario@gmail.com",
                "clave1234",
                "3001234567",
                "12345678",
                LocalDate.now().minusYears(20),
                "PATIENT",
                ""
        );
    }
}
