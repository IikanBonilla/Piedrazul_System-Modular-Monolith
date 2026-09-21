package com.groupsoft.piedrazul.user.application.usecase;

import com.groupsoft.piedrazul.user.application.dto.RegisterPatientRequest;
import com.groupsoft.piedrazul.user.application.exception.PatientRegistrationException;
import com.groupsoft.piedrazul.user.domain.model.Role;
import com.groupsoft.piedrazul.user.domain.model.User;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterPatientUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private RegisterPatientUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterPatientUseCase(userRepository);
    }

    @Test
    void shouldRegisterPatientWhenDataIsComplete() {
        RegisterPatientRequest request = validRequest();
        when(userRepository.findByUsername("ana")).thenReturn(Optional.empty());
        when(userRepository.findByDocumentNumber("1098765432")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(15L);
            return user;
        });

        var result = useCase.execute(request);

        assertEquals(15L, result.getId());
        assertEquals("ana", result.getUsername());
        assertTrue(result.getMessage().toLowerCase().contains("exitoso"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Role.PATIENT, captor.getValue().getRole());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void shouldRejectIncompleteRegistrationAndIndicateMissingFields() {
        RegisterPatientRequest request = RegisterPatientRequest.builder()
                .username("ana")
                .build();

        PatientRegistrationException ex = assertThrows(
                PatientRegistrationException.class,
                () -> useCase.execute(request)
        );

        assertEquals("INCOMPLETE_REGISTRATION", ex.getCode());
        assertTrue(ex.getMessage().contains("contrasena"));
        assertTrue(ex.getMessage().contains("nombre completo"));
        assertTrue(ex.getMessage().contains("documento"));
        assertTrue(ex.getMessage().contains("telefono"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenUsernameAlreadyExists() {
        when(userRepository.findByUsername("ana"))
                .thenReturn(Optional.of(User.builder().id(1L).username("ana").build()));

        PatientRegistrationException ex = assertThrows(
                PatientRegistrationException.class,
                () -> useCase.execute(validRequest())
        );

        assertEquals("USER_ALREADY_EXISTS", ex.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenDocumentAlreadyExists() {
        when(userRepository.findByUsername("ana")).thenReturn(Optional.empty());
        when(userRepository.findByDocumentNumber("1098765432"))
                .thenReturn(Optional.of(User.builder().id(2L).documentNumber("1098765432").build()));

        PatientRegistrationException ex = assertThrows(
                PatientRegistrationException.class,
                () -> useCase.execute(validRequest())
        );

        assertEquals("USER_ALREADY_EXISTS", ex.getCode());
        verify(userRepository, never()).save(any());
    }

    private RegisterPatientRequest validRequest() {
        return RegisterPatientRequest.builder()
                .username("ana")
                .password("clave123")
                .fullName("Ana Ruiz")
                .documentNumber("1098765432")
                .phone("3009998877")
                .build();
    }
}
