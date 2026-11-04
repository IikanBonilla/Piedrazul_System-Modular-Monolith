package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.enricher.AppointmentResponseEnricher;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSearchException;
import com.groupsoft.piedrazul.appointment.application.mapper.AppointmentAssembler;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import com.groupsoft.piedrazul.shared.dto.UserSummary;
import com.groupsoft.piedrazul.shared.port.UserQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchAppointmentsByPatientDocumentUseCaseTest {

    private static final UserSummary PATIENT =
            new UserSummary(10L, "Juan Pérez", "12345678", "3001234567");

    @Mock
    private AppointmentRepositoryPort appointmentRepository;

    @Mock
    private UserQueryPort userQueryPort;

    @Mock
    private AppointmentResponseEnricher enricher;

    private SearchAppointmentsByPatientDocumentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchAppointmentsByPatientDocumentUseCase(
                appointmentRepository,
                userQueryPort,
                new AppointmentAssembler(enricher)
        );
    }

    @Test
    void shouldReturnPatientHistoryWhenPatientHasAppointments() {
        Appointment first = appointment(1L, LocalDateTime.of(2026, 9, 2, 9, 0));
        Appointment second = appointment(2L, LocalDateTime.of(2026, 9, 9, 10, 0));

        when(userQueryPort.findByDocumentNumber("12345678")).thenReturn(Optional.of(PATIENT));
        when(appointmentRepository.findByPatientId(10L)).thenReturn(List.of(first, second));
        when(enricher.enrich(any(), any())).thenAnswer(invocation -> invocation.getArgument(1));

        var result = useCase.execute(" 12345678 ");

        assertEquals(10L, result.getPatientId());
        assertEquals("Juan Pérez", result.getPatientName());
        assertEquals("12345678", result.getPatientDocument());
        assertEquals(2, result.getTotal());
        assertEquals(List.of(1L, 2L), result.getAppointments().stream().map(a -> a.getId()).toList());
    }

    @Test
    void shouldReturnEmptyHistoryWhenPatientHasNoAppointments() {
        when(userQueryPort.findByDocumentNumber("12345678")).thenReturn(Optional.of(PATIENT));
        when(appointmentRepository.findByPatientId(10L)).thenReturn(List.of());

        var result = useCase.execute("12345678");

        assertEquals(0, result.getTotal());
        assertTrue(result.getAppointments().isEmpty());
        assertEquals("Juan Pérez", result.getPatientName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldThrowWhenDocumentIsMissing(String document) {
        AppointmentSearchException ex = assertThrows(AppointmentSearchException.class,
                () -> useCase.execute(document));

        assertEquals("MISSING_DOCUMENT", ex.getCode());
        verifyNoInteractions(userQueryPort, appointmentRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "1234567890123456", "12a45678", "123 45678", "-1234567"})
    void shouldThrowWhenDocumentIsInvalid(String document) {
        AppointmentSearchException ex = assertThrows(AppointmentSearchException.class,
                () -> useCase.execute(document));

        assertEquals("INVALID_DOCUMENT", ex.getCode());
        verifyNoInteractions(userQueryPort, appointmentRepository);
    }

    @Test
    void shouldThrowWhenPatientIsNotRegistered() {
        when(userQueryPort.findByDocumentNumber("99999999")).thenReturn(Optional.empty());

        AppointmentSearchException ex = assertThrows(AppointmentSearchException.class,
                () -> useCase.execute("99999999"));

        assertEquals("PATIENT_NOT_FOUND", ex.getCode());
        verifyNoInteractions(appointmentRepository);
    }

    private Appointment appointment(Long id, LocalDateTime date) {
        return Appointment.builder()
                .id(id)
                .patientId(10L)
                .doctorId(1L)
                .appointmentDate(date)
                .status(AppointmentStatus.CONFIRMED)
                .build();
    }
}
