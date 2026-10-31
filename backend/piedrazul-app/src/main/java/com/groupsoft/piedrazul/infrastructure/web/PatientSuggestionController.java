package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.user.application.dto.PatientSuggestionDTO;
import com.groupsoft.piedrazul.user.application.usecase.SuggestPatientsByDocumentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Busqueda de pacientes por cedula")
public class PatientSuggestionController {

    private final SuggestPatientsByDocumentUseCase suggestPatientsByDocumentUseCase;

    @GetMapping("/suggest")
    @Operation(summary = "Autocompletar pacientes activos por prefijo de cedula")
    public ResponseEntity<List<PatientSuggestionDTO>> suggest(
            @RequestParam(required = false) String document) {
        return ResponseEntity.ok(suggestPatientsByDocumentUseCase.execute(document));
    }
}
