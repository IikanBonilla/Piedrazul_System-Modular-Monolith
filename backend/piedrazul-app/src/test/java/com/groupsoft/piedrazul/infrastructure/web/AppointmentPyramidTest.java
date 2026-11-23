package com.groupsoft.piedrazul.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentPyramidTest {

    private static final String JUSTIFICATION = "No puedo asistir a la consulta";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void tokens() {
        when(jwtDecoder.decode(anyString())).thenAnswer(invocation -> {
            String raw = invocation.getArgument(0);
            if ("medico".equals(raw)) {
                return jwt("medico", "DOCTOR");
            }
            return jwt("paciente", "PATIENT");
        });
    }

    @Test
    @DisplayName("Integracion: el reporte anual sale de las citas guardadas")
    void reportCountsStoredAppointments() throws Exception {
        LocalDate demoDay = LocalDate.now().plusDays(1);
        int month = demoDay.getMonthValue() - 1;

        mockMvc.perform(get("/api/v1/appointments/report")
                        .param("year", String.valueOf(demoDay.getYear()))
                        .header("Authorization", "Bearer medico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.months.length()").value(12))
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.doctors[0].doctorName").value("Dra. Maria Lopez"))
                .andExpect(jsonPath("$.doctors[0].counts[" + month + "]").value(greaterThanOrEqualTo(2)));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Sistema: el paciente agenda, cancela y el horario vuelve a quedar libre")
    void patientCancelsAndTheSlotIsFreeAgain() throws Exception {
        long doctorId = doctorId();
        LocalDate date = bookableDate();
        String slot = firstSlot(doctorId, date);

        long appointmentId = book(doctorId, slot);

        mockMvc.perform(get("/api/v1/doctors/{id}/slots", doctorId)
                        .param("date", date.toString())
                        .header("Authorization", "Bearer paciente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots[*].start", not(hasItem(slot))));

        mockMvc.perform(post("/api/v1/appointments/{id}/cancel", appointmentId)
                        .header("Authorization", "Bearer paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"justification\":\"" + JUSTIFICATION + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.message").value(
                        "La cita quedó cancelada y el horario volvió a quedar disponible."));

        mockMvc.perform(get("/api/v1/doctors/{id}/slots", doctorId)
                        .param("date", date.toString())
                        .header("Authorization", "Bearer paciente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slots[*].start", hasItem(slot)));
    }

    @Test
    @DisplayName("Aceptacion: sin dos dias o sin justificacion la cita no cambia y el mensaje es claro")
    void cancellationRulesAreExplainedToThePatient() throws Exception {
        MvcResult mine = mockMvc.perform(get("/api/v1/appointments/mine")
                        .header("Authorization", "Bearer paciente"))
                .andExpect(status().isOk())
                .andReturn();
        int existingId = com.jayway.jsonpath.JsonPath.read(mine.getResponse().getContentAsString(), "$.appointments[0].id");

        mockMvc.perform(post("/api/v1/appointments/{id}/cancel", existingId)
                        .header("Authorization", "Bearer paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"justification\":\"" + JUSTIFICATION + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CANCELLATION_TOO_LATE"))
                .andExpect(jsonPath("$.message").value(
                        "Solo puedes cancelar una cita con al menos dos días de anticipación."));

        long doctorId = doctorId();
        LocalDate date = bookableDate();
        String slot = secondSlot(doctorId, date);
        long appointmentId = book(doctorId, slot);

        mockMvc.perform(post("/api/v1/appointments/{id}/cancel", appointmentId)
                        .header("Authorization", "Bearer paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"justification\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_JUSTIFICATION"))
                .andExpect(jsonPath("$.message").value("La justificación es obligatoria."));

        mockMvc.perform(get("/api/v1/appointments/mine")
                        .header("Authorization", "Bearer paciente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments[?(@.id == " + appointmentId + ")].status", hasItem("CONFIRMED")));
    }

    private long book(long doctorId, String slot) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentNumber\":\"1234567890\",\"doctorId\":" + doctorId
                                + ",\"slot\":\"" + slot + "\",\"confirmed\":true}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long doctorId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/doctors")
                        .header("Authorization", "Bearer paciente"))
                .andExpect(status().isOk())
                .andReturn();
        return ((Number) com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$[0].id")).longValue();
    }

    private String firstSlot(long doctorId, LocalDate date) throws Exception {
        return slotAt(doctorId, date, 0);
    }

    private String secondSlot(long doctorId, LocalDate date) throws Exception {
        return slotAt(doctorId, date, 1);
    }

    private String slotAt(long doctorId, LocalDate date, int index) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/doctors/{id}/slots", doctorId)
                        .param("date", date.toString())
                        .header("Authorization", "Bearer paciente"))
                .andExpect(status().isOk())
                .andReturn();
        List<String> slots = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.slots[*].start");
        if (slots.size() <= index) {
            throw new AssertionError("No hay suficientes franjas el " + date);
        }
        return slots.get(index);
    }

    private LocalDate bookableDate() {
        LocalDate date = LocalDate.now().plusDays(2);
        while (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private Jwt jwt(String username, String role) {
        return Jwt.withTokenValue(username)
                .header("alg", "none")
                .subject(username)
                .claim("preferred_username", username)
                .claim("realm_access", Map.of("roles", List.of(role)))
                .issuedAt(Instant.parse("2026-11-23T12:00:00Z"))
                .expiresAt(Instant.parse("2026-11-23T20:00:00Z"))
                .build();
    }
}
