package com.groupsoft.piedrazul.infrastructure.security;

import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSchedulingException;
import com.groupsoft.piedrazul.appointment.application.usecase.ExportAppointmentsCsvUseCase;
import com.groupsoft.piedrazul.appointment.application.usecase.QueryAvailableSlotsUseCase;
import com.groupsoft.piedrazul.appointment.application.usecase.ScheduleAppointmentUseCase;
import com.groupsoft.piedrazul.appointment.application.usecase.SearchAppointmentsByDoctorAndDateUseCase;
import com.groupsoft.piedrazul.appointment.application.usecase.SearchAppointmentsByPatientDocumentUseCase;
import com.groupsoft.piedrazul.availability.application.service.DoctorQueryService;
import com.groupsoft.piedrazul.availability.application.usecase.ConfigureDoctorSchedulingUseCase;
import com.groupsoft.piedrazul.availability.application.usecase.GetDoctorSchedulingConfigUseCase;
import com.groupsoft.piedrazul.infrastructure.config.CorsConfig;
import com.groupsoft.piedrazul.infrastructure.service.RegisterAccountService;
import com.groupsoft.piedrazul.infrastructure.web.AppointmentSchedulingController;
import com.groupsoft.piedrazul.infrastructure.web.AppointmentSearchController;
import com.groupsoft.piedrazul.infrastructure.web.AuthController;
import com.groupsoft.piedrazul.infrastructure.web.AvailabilitySlotsController;
import com.groupsoft.piedrazul.infrastructure.web.DoctorController;
import com.groupsoft.piedrazul.infrastructure.web.DoctorSchedulingConfigController;
import com.groupsoft.piedrazul.infrastructure.web.PatientRegistrationController;
import com.groupsoft.piedrazul.user.application.dto.SessionProfile;
import com.groupsoft.piedrazul.user.domain.model.Role;
import com.groupsoft.piedrazul.user.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AppointmentSchedulingController.class,
        AppointmentSearchController.class,
        AvailabilitySlotsController.class,
        DoctorController.class,
        DoctorSchedulingConfigController.class,
        AuthController.class,
        PatientRegistrationController.class
})
@Import({SecurityConfig.class, CorsConfig.class})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ScheduleAppointmentUseCase scheduleAppointmentUseCase;

    @MockBean
    private QueryAvailableSlotsUseCase queryAvailableSlotsUseCase;

    @MockBean
    private SearchAppointmentsByDoctorAndDateUseCase searchAppointmentsByDoctorAndDateUseCase;

    @MockBean
    private ExportAppointmentsCsvUseCase exportAppointmentsCsvUseCase;

    @MockBean
    private SearchAppointmentsByPatientDocumentUseCase searchAppointmentsByPatientDocumentUseCase;

    @MockBean
    private DoctorQueryService doctorQueryService;

    @MockBean
    private ConfigureDoctorSchedulingUseCase configureDoctorSchedulingUseCase;

    @MockBean
    private GetDoctorSchedulingConfigUseCase getDoctorSchedulingConfigUseCase;

    @MockBean
    private RegisterAccountService registerAccountService;

    @MockBean
    private CurrentSessionService currentSessionService;

    @Test
    void missingTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/doctors"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void schedulerCanQueryAppointmentsAndPatientCannot() throws Exception {
        when(jwtDecoder.decode("scheduler")).thenReturn(token("SCHEDULER"));
        when(jwtDecoder.decode("patient")).thenReturn(token("PATIENT"));

        mockMvc.perform(get("/api/v1/appointments/doctor/1").header("Authorization", "Bearer scheduler"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/appointments/doctor/1").header("Authorization", "Bearer patient"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void patientCanBookAndReadSlotsAndSchedulerCannotBook() throws Exception {
        when(jwtDecoder.decode("patient")).thenReturn(token("PATIENT"));
        when(jwtDecoder.decode("scheduler")).thenReturn(token("SCHEDULER"));
        when(currentSessionService.profile(any())).thenReturn(patientSession());

        mockMvc.perform(post("/api/v1/appointments").header("Authorization", "Bearer patient"))
                .andExpect(status().isCreated());
        verify(scheduleAppointmentUseCase).execute(eq(10L), any());
        mockMvc.perform(get("/api/v1/doctors/4/slots").header("Authorization", "Bearer patient"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/appointments").header("Authorization", "Bearer scheduler"))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCannotBookForAnotherPatient() throws Exception {
        when(jwtDecoder.decode("patient")).thenReturn(token("PATIENT"));
        when(currentSessionService.profile(any())).thenReturn(patientSession());
        when(scheduleAppointmentUseCase.execute(eq(10L), any()))
                .thenThrow(AppointmentSchedulingException.patientMismatch());

        mockMvc.perform(post("/api/v1/appointments")
                        .header("Authorization", "Bearer patient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentNumber\":\"9876543210\",\"doctorId\":1,"
                                + "\"slot\":\"2026-09-21T11:00:00\",\"confirmed\":true}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PATIENT_MISMATCH"));
    }

    @Test
    void onlyAdministratorChangesSchedulingConfig() throws Exception {
        when(jwtDecoder.decode("admin")).thenReturn(token("ADMINISTRATOR"));
        when(jwtDecoder.decode("scheduler")).thenReturn(token("SCHEDULER"));
        when(jwtDecoder.decode("patient")).thenReturn(token("PATIENT"));

        mockMvc.perform(put("/api/v1/admin/doctors/4/scheduling-config").header("Authorization", "Bearer admin"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/doctors/4/scheduling-config").header("Authorization", "Bearer scheduler"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/admin/doctors/4/scheduling-config").header("Authorization", "Bearer patient"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrationStaysPublic() throws Exception {
        when(registerAccountService.register(any())).thenReturn(User.builder()
                .id(3L)
                .username("ana")
                .fullName("Ana Ruiz")
                .role(Role.PATIENT)
                .active(true)
                .build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/patients/register")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    private SessionProfile patientSession() {
        return new SessionProfile(0L, 10L, "patient", "Juan Perez", "PATIENT", null, null, null);
    }

    private Jwt token(String role) {
        return Jwt.withTokenValue(role)
                .header("alg", "none")
                .subject(role.toLowerCase())
                .claim("preferred_username", role.toLowerCase())
                .claim("realm_access", Map.of("roles", List.of(role)))
                .issuedAt(Instant.parse("2026-10-19T00:00:00Z"))
                .expiresAt(Instant.parse("2026-10-19T08:00:00Z"))
                .build();
    }
}
