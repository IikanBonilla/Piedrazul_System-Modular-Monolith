package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentSearchResultDTO;
import com.groupsoft.piedrazul.appointment.application.dto.PatientAppointmentHistoryDTO;
import com.groupsoft.piedrazul.appointment.application.usecase.ExportAppointmentsCsvUseCase;
import com.groupsoft.piedrazul.appointment.application.usecase.SearchAppointmentsByDoctorAndDateUseCase;
import com.groupsoft.piedrazul.appointment.application.usecase.SearchAppointmentsByPatientDocumentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Consulta, exportacion e historial de citas")
public class AppointmentSearchController {

    private final SearchAppointmentsByDoctorAndDateUseCase searchUseCase;
    private final ExportAppointmentsCsvUseCase exportUseCase;
    private final SearchAppointmentsByPatientDocumentUseCase historyUseCase;

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Buscar citas por medico y fecha")
    public ResponseEntity<AppointmentSearchResultDTO> searchByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(searchUseCase.execute(doctorId, date));
    }

    @GetMapping(value = "/doctor/{doctorId}/export", produces = "text/csv")
    @Operation(summary = "Exportar citas de un medico o terapista en una fecha (CSV)")
    public ResponseEntity<byte[]> exportByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        byte[] csv = exportUseCase.execute(doctorId, date);
        String filename = "citas-" + doctorId + "-" + (date == null ? "sin-fecha" : date) + ".csv";
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    @GetMapping("/patient")
    @Operation(summary = "Historial de citas de un paciente por cedula")
    public ResponseEntity<PatientAppointmentHistoryDTO> searchByPatientDocument(
            @RequestParam(required = false) String document) {

        return ResponseEntity.ok(historyUseCase.execute(document));
    }
}
