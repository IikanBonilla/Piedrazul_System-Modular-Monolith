package com.groupsoft.piedrazul.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private FilterChain filterChain;

    private AuthFilter authFilter;

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter(authTokenService, new ObjectMapper());
    }

    @Test
    void allowsPublicRegistrationWithoutToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/register");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void allowsLegacyPatientRegistrationWithoutToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/patients/register");
        MockHttpServletResponse response = new MockHttpServletResponse();

        authFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void allowsSchedulerToQueryAppointments() throws Exception {
        assertRoleCanPass("GET", "/api/v1/appointments/doctor/1", "SCHEDULER");
    }

    @Test
    void rejectsPatientQueryingAppointments() throws Exception {
        assertRoleIsForbidden("GET", "/api/v1/appointments/doctor/1", "PATIENT");
    }

    @Test
    void allowsPatientToBookAndQuerySlots() throws Exception {
        assertRoleCanPass("POST", "/api/v1/appointments", "PATIENT");
        assertRoleCanPass("GET", "/api/v1/doctors/4/slots", "PATIENT");
    }

    @Test
    void rejectsSchedulerBookingAppointments() throws Exception {
        assertRoleIsForbidden("POST", "/api/v1/appointments", "SCHEDULER");
    }

    @Test
    void allowsOnlyAdministratorToChangeSchedulingConfig() throws Exception {
        assertRoleCanPass("PUT", "/api/v1/admin/doctors/4/scheduling-config", "ADMINISTRATOR");
        assertRoleIsForbidden("GET", "/api/v1/admin/doctors/4/scheduling-config", "SCHEDULER");
        assertRoleIsForbidden("PUT", "/api/v1/admin/doctors/4/scheduling-config", "PATIENT");
    }

    private void assertRoleCanPass(String method, String path, String role) throws Exception {
        MockHttpServletRequest request = authorized(method, path, role);
        MockHttpServletResponse response = new MockHttpServletResponse();

        authFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    private void assertRoleIsForbidden(String method, String path, String role) throws Exception {
        MockHttpServletRequest request = authorized(method, path, role);
        MockHttpServletResponse response = new MockHttpServletResponse();

        authFilter.doFilter(request, response, filterChain);

        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    private MockHttpServletRequest authorized(String method, String path, String role) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.addHeader("Authorization", "Bearer token-" + role);
        when(authTokenService.resolve("token-" + role)).thenReturn(Optional.of(
                new IssuedToken("token-" + role, System.currentTimeMillis() + 60_000, 1L, role)
        ));
        return request;
    }
}
