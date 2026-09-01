package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.enricher.AppointmentResponseEnricher;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSearchException;
import com.groupsoft.piedrazul.appointment.application.mapper.AppointmentAssembler;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import com.groupsoft.piedrazul.shared.dto.DoctorSummary;
import com.groupsoft.piedrazul.shared.port.DoctorQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchAppointmentsByDoctorAndDateUseCaseTest {

    @Mock
    private AppointmentRepositoryPort appointmentRepository;

    @Mock
    private DoctorQueryPort doctorQueryPort;

    @Mock
    private AppointmentResponseEnricher enricher;

    private SearchAppointmentsByDoctorAndDateUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchAppointmentsByDoctorAndDateUseCase(
                appointmentRepository,
                doctorQueryPort,
                new AppointmentAssembler(enricher)
        );
    }

    @Test
    void shouldReturnAppointmentsWhenSearchIsValid() {
        LocalDate date = LocalDate.of(2026, 9, 2);
        Appointment appointment = Appointment.builder()
                .id(1L)
                .patientId(10L)
                .doctorId(1L)
                .appointmentDate(date.atTime(9, 0))
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(doctorQueryPort.findById(1L))
                .thenReturn(Optional.of(new DoctorSummary(1L, "Dra. Maria Lopez", "Medicina General")));
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(appointment));
        when(enricher.enrich(any(), any())).thenAnswer(invocation -> invocation.getArgument(1));

        var result = useCase.execute(1L, date);

        assertEquals(1, result.getTotal());
        assertEquals("Dra. Maria Lopez", result.getDoctorName());
        assertEquals(1, result.getAppointments().size());
    }

    @Test
    void shouldReturnEmptyListWhenNoAppointmentsExist() {
        LocalDate date = LocalDate.of(2026, 9, 2);

        when(doctorQueryPort.findById(1L))
                .thenReturn(Optional.of(new DoctorSummary(1L, "Dra. Maria Lopez", "Medicina General")));
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of());

        var result = useCase.execute(1L, date);

        assertEquals(0, result.getTotal());
        assertTrue(result.getAppointments().isEmpty());
    }

    @Test
    void shouldThrowWhenDateIsMissing() {
        assertThrows(AppointmentSearchException.class, () -> useCase.execute(1L, null));
    }

    @Test
    void shouldThrowWhenDoctorIsMissing() {
        assertThrows(AppointmentSearchException.class, () -> useCase.execute(null, LocalDate.now()));
    }

    // TODO (tu turno): agrega prueba para DOCTOR_NOT_FOUND (HU-1.1 escenario medico inexistente)
}
