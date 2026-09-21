package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.user.application.dto.RegisterPatientRequest;
import com.groupsoft.piedrazul.user.application.dto.RegisterPatientResponseDTO;
import com.groupsoft.piedrazul.user.application.usecase.RegisterPatientUseCase;
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

    private final RegisterPatientUseCase registerPatientUseCase;

    @PostMapping("/register")
    @Operation(summary = "Registrar paciente (HU-2.2)")
    public ResponseEntity<RegisterPatientResponseDTO> register(
            @RequestBody(required = false) RegisterPatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerPatientUseCase.execute(request));
    }
}
