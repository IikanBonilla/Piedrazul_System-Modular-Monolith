package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.availability.application.dto.DoctorSchedulingConfigDTO;
import com.groupsoft.piedrazul.availability.application.usecase.ConfigureDoctorSchedulingUseCase;
import com.groupsoft.piedrazul.availability.application.usecase.GetDoctorSchedulingConfigUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/doctors")
@RequiredArgsConstructor
@Tag(name = "Admin scheduling", description = "HE-03: Configuracion de parametros del sistema")
public class DoctorSchedulingConfigController {

    private final ConfigureDoctorSchedulingUseCase configureDoctorSchedulingUseCase;
    private final GetDoctorSchedulingConfigUseCase getDoctorSchedulingConfigUseCase;

    @GetMapping("/{doctorId}/scheduling-config")
    @Operation(summary = "Consultar configuracion de agendamiento (HU-3.5)")
    public ResponseEntity<DoctorSchedulingConfigDTO> get(@PathVariable Long doctorId) {
        return ResponseEntity.ok(getDoctorSchedulingConfigUseCase.execute(doctorId));
    }

    @PutMapping("/{doctorId}/scheduling-config")
    @Operation(summary = "Guardar configuracion de agendamiento (HU-3.1 a HU-3.5)")
    public ResponseEntity<DoctorSchedulingConfigDTO> put(
            @PathVariable Long doctorId,
            @RequestBody(required = false) DoctorSchedulingConfigDTO request) {
        return ResponseEntity.ok(configureDoctorSchedulingUseCase.execute(doctorId, request));
    }
}
