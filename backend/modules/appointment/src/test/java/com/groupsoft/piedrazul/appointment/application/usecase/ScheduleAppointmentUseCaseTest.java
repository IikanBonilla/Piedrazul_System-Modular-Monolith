package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.ScheduleAppointmentRequest;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSchedulingException;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import com.groupsoft.piedrazul.shared.dto.DoctorSummary;
import com.groupsoft.piedrazul.shared.dto.UserSummary;
import com.groupsoft.piedrazul.shared.port.DoctorQueryPort;
import com.groupsoft.piedrazul.shared.port.DoctorSchedulePort;
import com.groupsoft.piedrazul.shared.port.UserQueryPort;
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
class ScheduleAppointmentUseCaseTest {

    @Mock
    private AppointmentRepositoryPort appointmentRepository;

    @Mock
    private DoctorQueryPort doctorQueryPort;

    @Mock
    private UserQueryPort userQueryPort;

    @Mock
    private DoctorSchedulePort doctorSchedulePort;

    private ScheduleAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ScheduleAppointmentUseCase(
                appointmentRepository, doctorQueryPort, userQueryPort, doctorSchedulePort);
    }

    @Test
    void shouldScheduleAppointmentWhenSlotIsAvailableAndConfirmed() {
        LocalDate date = LocalDate.of(2026, 9, 21);
        LocalDateTime slot = date.atTime(11, 0);
        stubHappyPath(date);
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any())).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            appointment.setId(99L);
            return appointment;
        });

        var result = useCase.execute(validRequest(slot, true));

        assertEquals(99L, result.getId());
        assertEquals("Juan Perez", result.getPatientName());
        assertEquals("Dra. Maria Lopez", result.getDoctorName());
        assertEquals(slot, result.getAppointmentDate());
        assertEquals(AppointmentStatus.CONFIRMED, result.getStatus());
        assertTrue(result.getMessage().contains("21/09/2026"));
        assertTrue(result.getMessage().contains("11:00"));
        assertTrue(result.getMessage().contains("Dra. Maria Lopez"));
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldRequireExplicitConfirmation() {
        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(validRequest(LocalDateTime.of(2026, 9, 21, 11, 0), false))
        );
        assertEquals("CONFIRMATION_REQUIRED", ex.getCode());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldRejectOccupiedSlot() {
        LocalDate date = LocalDate.of(2026, 9, 21);
        LocalDateTime occupied = date.atTime(9, 0);
        stubHappyPath(date);
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(Appointment.builder()
                        .doctorId(1L)
                        .appointmentDate(occupied)
                        .status(AppointmentStatus.CONFIRMED)
                        .build()));

        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(validRequest(occupied, true))
        );

        assertEquals("SLOT_NOT_AVAILABLE", ex.getCode());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldInformWhenThereAreNoAvailableSlots() {
        LocalDate date = LocalDate.of(2026, 9, 21);
        LocalDateTime slot = date.atTime(9, 0);
        stubHappyPath(date);
        when(doctorSchedulePort.candidateSlots(1L, date)).thenReturn(List.of(slot));
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(Appointment.builder()
                        .doctorId(1L)
                        .appointmentDate(slot)
                        .status(AppointmentStatus.PENDING)
                        .build()));

        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(validRequest(slot, true))
        );

        assertEquals("NO_AVAILABLE_SLOTS", ex.getCode());
    }

    private void stubHappyPath(LocalDate date) {
        when(userQueryPort.findByDocumentNumber("1234567890"))
                .thenReturn(Optional.of(new UserSummary(10L, "Juan Perez", "1234567890", "3001234567")));
        when(doctorQueryPort.findById(1L))
                .thenReturn(Optional.of(new DoctorSummary(1L, "Dra. Maria Lopez", "Medicina General")));
        when(doctorSchedulePort.candidateSlots(1L, date)).thenReturn(List.of(
                date.atTime(9, 0),
                date.atTime(11, 0)
        ));
    }

    private ScheduleAppointmentRequest validRequest(LocalDateTime slot, boolean confirmed) {
        return ScheduleAppointmentRequest.builder()
                .documentNumber("1234567890")
                .doctorId(1L)
                .slot(slot)
                .confirmed(confirmed)
                .build();
    }
}
