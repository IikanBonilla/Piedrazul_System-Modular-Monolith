package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.appointment.application.dto.AvailableSlotsResultDTO;
import com.groupsoft.piedrazul.appointment.application.usecase.QueryAvailableSlotsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "HE-02/HE-03: Franjas disponibles")
public class AvailabilitySlotsController {

    private final QueryAvailableSlotsUseCase queryAvailableSlotsUseCase;

    @GetMapping("/{doctorId}/slots")
    @Operation(summary = "Consultar franjas disponibles por medico y fecha (HU-2.3 / HU-3.5)")
    public ResponseEntity<AvailableSlotsResultDTO> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(queryAvailableSlotsUseCase.execute(doctorId, date));
    }
}
