package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSchedulingException;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelAppointmentUseCaseTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 23, 9, 0);
    private static final String REASON = "No puedo asistir a la consulta";

    @Mock
    private AppointmentRepositoryPort appointmentRepository;

    private CancelAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelAppointmentUseCase(appointmentRepository);
    }

    @Test
    void cancelsOwnAppointmentTwoDaysAheadAndFreesTheSlot() {
        Appointment appointment = appointment(NOW.plusDays(2), AppointmentStatus.CONFIRMED, 10L);
        when(appointmentRepository.findById(8L)).thenReturn(Optional.of(appointment));

        var result = useCase.execute(10L, 8L, REASON, NOW);

        assertEquals("CANCELLED", result.status());
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertEquals(REASON, appointment.getCancellationReason());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void rejectsACancellationOneDayAhead() {
        Appointment appointment = appointment(NOW.plusDays(1), AppointmentStatus.PENDING, 10L);
        when(appointmentRepository.findById(8L)).thenReturn(Optional.of(appointment));

        AppointmentSchedulingException error = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(10L, 8L, REASON, NOW)
        );

        assertEquals("CANCELLATION_TOO_LATE", error.getCode());
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());
    }

    @Test
    void rejectsABlankJustification() {
        AppointmentSchedulingException error = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(10L, 8L, "   ", NOW)
        );

        assertEquals("MISSING_JUSTIFICATION", error.getCode());
    }

    @Test
    void rejectsAnotherPatientsAppointment() {
        Appointment appointment = appointment(NOW.plusDays(3), AppointmentStatus.CONFIRMED, 99L);
        when(appointmentRepository.findById(8L)).thenReturn(Optional.of(appointment));

        AppointmentSchedulingException error = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(10L, 8L, REASON, NOW)
        );

        assertEquals("APPOINTMENT_FORBIDDEN", error.getCode());
    }

    @Test
    void rejectsAnAlreadyCancelledAppointment() {
        Appointment appointment = appointment(NOW.plusDays(4), AppointmentStatus.CANCELLED, 10L);
        when(appointmentRepository.findById(8L)).thenReturn(Optional.of(appointment));

        AppointmentSchedulingException error = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(10L, 8L, REASON, NOW)
        );

        assertEquals("APPOINTMENT_NOT_CANCELLABLE", error.getCode());
    }

    private Appointment appointment(LocalDateTime when, AppointmentStatus status, Long patientId) {
        return Appointment.builder()
                .id(8L)
                .patientId(patientId)
                .doctorId(1L)
                .appointmentDate(when)
                .status(status)
                .build();
    }
}
