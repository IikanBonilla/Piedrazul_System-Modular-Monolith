package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.infrastructure.service.RegisterAccountService;
import com.groupsoft.piedrazul.user.application.dto.RegisterPatientRequest;
import com.groupsoft.piedrazul.user.application.dto.RegisterPatientResponseDTO;
import com.groupsoft.piedrazul.user.application.dto.RegisterUserRequest;
import com.groupsoft.piedrazul.user.domain.model.Role;
import com.groupsoft.piedrazul.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientRegistrationControllerTest {

    @Mock
    private RegisterAccountService registerAccountService;

    private PatientRegistrationController controller;

    @BeforeEach
    void setUp() {
        controller = new PatientRegistrationController(registerAccountService);
    }

    @Test
    void registersPatientThroughUnifiedAccountService() {
        RegisterPatientRequest request = RegisterPatientRequest.builder()
                .username("ana.ruiz")
                .password("clave123")
                .fullName("Ana Ruiz")
                .email("ana@example.com")
                .documentNumber("1098765432")
                .phone("3009998877")
                .birthDate(LocalDate.of(1998, 3, 4))
                .build();
        when(registerAccountService.register(org.mockito.ArgumentMatchers.any())).thenReturn(User.builder()
                .id(15L)
                .username("ana.ruiz")
                .fullName("Ana Ruiz")
                .documentNumber("1098765432")
                .phone("3009998877")
                .role(Role.PATIENT)
                .active(true)
                .build());

        ResponseEntity<RegisterPatientResponseDTO> response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(15L, response.getBody().getId());
        assertEquals("ana.ruiz", response.getBody().getUsername());
        assertTrue(response.getBody().getMessage().toLowerCase().contains("exitoso"));

        ArgumentCaptor<RegisterUserRequest> captor = ArgumentCaptor.forClass(RegisterUserRequest.class);
        verify(registerAccountService).register(captor.capture());
        RegisterUserRequest unified = captor.getValue();
        assertEquals("PATIENT", unified.role());
        assertEquals("ana.ruiz", unified.username());
        assertEquals("ana@example.com", unified.email());
        assertEquals("1098765432", unified.documentNumber());
        assertEquals(LocalDate.of(1998, 3, 4), unified.birthDate());
    }
}
