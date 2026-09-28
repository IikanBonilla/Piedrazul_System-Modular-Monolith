package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.infrastructure.service.RegisterAccountService;
import com.groupsoft.piedrazul.user.application.dto.RegisterPatientRequest;
import com.groupsoft.piedrazul.user.application.dto.RegisterPatientResponseDTO;
import com.groupsoft.piedrazul.user.application.dto.RegisterUserRequest;
import com.groupsoft.piedrazul.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "HE-02: Registro de pacientes")
public class PatientRegistrationController {

    private final RegisterAccountService registerAccountService;

    @PostMapping("/register")
    @Operation(summary = "Registrar paciente (HU-2.2) usando el registro unificado")
    public ResponseEntity<RegisterPatientResponseDTO> register(
            @RequestBody(required = false) RegisterPatientRequest request) {
        User user = registerAccountService.register(toUnifiedRequest(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterPatientResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .documentNumber(user.getDocumentNumber())
                .phone(user.getPhone())
                .message("Registro exitoso. El paciente ya puede agendar citas.")
                .build());
    }

    private RegisterUserRequest toUnifiedRequest(RegisterPatientRequest request) {
        RegisterPatientRequest source = request == null ? RegisterPatientRequest.builder().build() : request;
        return new RegisterUserRequest(
                source.getFullName(),
                source.getUsername(),
                source.getEmail(),
                source.getPassword(),
                source.getPhone(),
                source.getDocumentNumber(),
                source.getBirthDate(),
                "PATIENT",
                null
        );
    }
}
