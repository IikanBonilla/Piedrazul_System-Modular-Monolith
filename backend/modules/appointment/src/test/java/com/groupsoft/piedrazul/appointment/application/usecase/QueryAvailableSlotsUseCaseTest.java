package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSchedulingException;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import com.groupsoft.piedrazul.shared.dto.DoctorSummary;
import com.groupsoft.piedrazul.shared.port.DoctorQueryPort;
import com.groupsoft.piedrazul.shared.port.DoctorSchedulePort;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryAvailableSlotsUseCaseTest {

    @Mock
    private AppointmentRepositoryPort appointmentRepository;

    @Mock
    private DoctorQueryPort doctorQueryPort;

    @Mock
    private DoctorSchedulePort doctorSchedulePort;

    private QueryAvailableSlotsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new QueryAvailableSlotsUseCase(
                appointmentRepository, doctorQueryPort, doctorSchedulePort);
    }

    @Test
    void shouldReturnOnlyUnoccupiedSlots() {
        LocalDate date = LocalDate.of(2026, 9, 21);
        when(doctorQueryPort.findById(1L))
                .thenReturn(Optional.of(new DoctorSummary(1L, "Dra. Maria Lopez", "Medicina General")));
        when(doctorSchedulePort.candidateSlots(date)).thenReturn(List.of(
                date.atTime(9, 0),
                date.atTime(9, 30),
                date.atTime(10, 30)
        ));
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(
                        occupied(date.atTime(9, 0), AppointmentStatus.CONFIRMED),
                        occupied(date.atTime(10, 30), AppointmentStatus.CANCELLED)
                ));

        var result = useCase.execute(1L, date);

        assertEquals(2, result.getTotal());
        assertEquals(date.atTime(9, 30), result.getSlots().get(0).getStart());
        assertEquals(date.atTime(10, 30), result.getSlots().get(1).getStart());
        assertNull(result.getMessage());
    }

    @Test
    void shouldInformWhenThereAreNoAvailableSlots() {
        LocalDate date = LocalDate.of(2026, 9, 21);
        when(doctorQueryPort.findById(1L))
                .thenReturn(Optional.of(new DoctorSummary(1L, "Dra. Maria Lopez", "Medicina General")));
        when(doctorSchedulePort.candidateSlots(date)).thenReturn(List.of(date.atTime(9, 0)));
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(occupied(date.atTime(9, 0), AppointmentStatus.PENDING)));

        var result = useCase.execute(1L, date);

        assertEquals(0, result.getTotal());
        assertTrue(result.getSlots().isEmpty());
        assertEquals(
                "No existen franjas disponibles para el profesional en la fecha seleccionada.",
                result.getMessage()
        );
    }

    @Test
    void shouldThrowWhenDoctorIsMissing() {
        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(null, LocalDate.now())
        );
        assertEquals("MISSING_DOCTOR", ex.getCode());
    }

    @Test
    void shouldThrowWhenDateIsMissing() {
        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(1L, null)
        );
        assertEquals("MISSING_DATE", ex.getCode());
    }

    private Appointment occupied(LocalDateTime when, AppointmentStatus status) {
        return Appointment.builder()
                .id(1L)
                .patientId(10L)
                .doctorId(1L)
                .appointmentDate(when)
                .status(status)
                .build();
    }
}
