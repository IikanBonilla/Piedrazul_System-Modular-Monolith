package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.appointment.application.dto.ScheduleAppointmentConfirmationDTO;
import com.groupsoft.piedrazul.appointment.application.dto.ScheduleAppointmentRequest;
import com.groupsoft.piedrazul.appointment.application.usecase.ScheduleAppointmentUseCase;
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
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "HE-01 consulta y HE-02 agendamiento")
public class AppointmentSchedulingController {

    private final ScheduleAppointmentUseCase scheduleAppointmentUseCase;

    @PostMapping
    @Operation(summary = "Agendar cita por el paciente (HU-2.1)")
    public ResponseEntity<ScheduleAppointmentConfirmationDTO> schedule(
            @RequestBody(required = false) ScheduleAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleAppointmentUseCase.execute(request));
    }
}
