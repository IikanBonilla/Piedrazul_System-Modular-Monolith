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
    void hu31_shouldHideDatesOutsideBookingWindow() {
        LocalDate date = LocalDate.of(2026, 11, 1);
        stubDoctor();
        when(doctorSchedulePort.isDateWithinBookingWindow(1L, date)).thenReturn(false);

        var result = useCase.execute(1L, date);

        assertEquals(0, result.getTotal());
        assertTrue(result.getSlots().isEmpty());
        assertTrue(result.getMessage().toLowerCase().contains("ventana"));
    }

    @Test
    void hu32_shouldOnlyReturnSlotsFromConfiguredWorkingDays() {
        LocalDate sunday = LocalDate.of(2026, 9, 20);
        stubDoctor();
        when(doctorSchedulePort.isDateWithinBookingWindow(1L, sunday)).thenReturn(true);
        when(doctorSchedulePort.candidateSlots(1L, sunday)).thenReturn(List.of());
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any())).thenReturn(List.of());

        var result = useCase.execute(1L, sunday);

        assertEquals(0, result.getTotal());
        assertTrue(result.getMessage().contains("No existen franjas disponibles"));
    }

    @Test
    void hu33_and_hu35_shouldUseDynamicCandidateSlotsFromConfig() {
        LocalDate date = LocalDate.of(2026, 9, 21);
        stubDoctor();
        when(doctorSchedulePort.isDateWithinBookingWindow(1L, date)).thenReturn(true);
        when(doctorSchedulePort.candidateSlots(1L, date)).thenReturn(List.of(
                date.atTime(8, 0),
                date.atTime(8, 20)
        ));
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any())).thenReturn(List.of());

        var result = useCase.execute(1L, date);

        assertEquals(2, result.getTotal());
        assertEquals(date.atTime(8, 0), result.getSlots().get(0).getStart());
        assertEquals(date.atTime(8, 20), result.getSlots().get(1).getStart());
        assertNull(result.getMessage());
    }

    @Test
    void hu34_shouldExcludeOccupiedSlotsFromConfiguredInterval() {
        LocalDate date = LocalDate.of(2026, 9, 21);
        stubDoctor();
        when(doctorSchedulePort.isDateWithinBookingWindow(1L, date)).thenReturn(true);
        when(doctorSchedulePort.candidateSlots(1L, date)).thenReturn(List.of(
                date.atTime(9, 0),
                date.atTime(9, 30)
        ));
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(Appointment.builder()
                        .doctorId(1L)
                        .appointmentDate(date.atTime(9, 0))
                        .status(AppointmentStatus.CONFIRMED)
                        .build()));

        var result = useCase.execute(1L, date);

        assertEquals(1, result.getTotal());
        assertEquals(date.atTime(9, 30), result.getSlots().get(0).getStart());
    }

    @Test
    void shouldThrowWhenDoctorOrDateIsMissing() {
        assertEquals("MISSING_DOCTOR",
                assertThrows(AppointmentSchedulingException.class, () -> useCase.execute(null, LocalDate.now())).getCode());
        assertEquals("MISSING_DATE",
                assertThrows(AppointmentSchedulingException.class, () -> useCase.execute(1L, null)).getCode());
    }

    private void stubDoctor() {
        when(doctorQueryPort.findById(1L))
                .thenReturn(Optional.of(new DoctorSummary(1L, "Dra. Maria Lopez", "Medicina General")));
    }
}
