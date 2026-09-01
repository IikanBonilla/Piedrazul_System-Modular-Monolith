package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentSearchResultDTO;
import com.groupsoft.piedrazul.appointment.application.usecase.SearchAppointmentsByDoctorAndDateUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "HE-01: Consulta de citas medicas")
public class AppointmentSearchController {

    private final SearchAppointmentsByDoctorAndDateUseCase searchUseCase;

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Buscar citas por medico y fecha (HU-1.1)")
    public ResponseEntity<AppointmentSearchResultDTO> searchByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(searchUseCase.execute(doctorId, date));
    }
}
