package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.AvailableSlotsResultDTO;
import com.groupsoft.piedrazul.appointment.application.dto.ScheduleAppointmentRequest;
import com.groupsoft.piedrazul.appointment.application.dto.TimeSlotDTO;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSchedulingException;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleAppointmentUseCaseTest {

    @Mock
    private AppointmentRepositoryPort appointmentRepository;

    @Mock
    private DoctorQueryPort doctorQueryPort;

    @Mock
    private DoctorSchedulePort doctorSchedulePort;

    @Mock
    private QueryAvailableSlotsUseCase queryAvailableSlotsUseCase;

    private ScheduleAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ScheduleAppointmentUseCase(
                appointmentRepository, doctorQueryPort, doctorSchedulePort, queryAvailableSlotsUseCase);
    }

    @Test
    void shouldRejectSlotThatIsNotInConfiguredAvailability() {
        LocalDateTime slot = LocalDate.of(2026, 9, 21).atTime(13, 0);
        when(doctorQueryPort.findById(1L))
                .thenReturn(Optional.of(new DoctorSummary(1L, "Dra. Maria Lopez", "Medicina General")));
        when(queryAvailableSlotsUseCase.execute(1L, slot.toLocalDate()))
                .thenReturn(AvailableSlotsResultDTO.builder()
                        .slots(List.of(TimeSlotDTO.builder().start(slot.toLocalDate().atTime(8, 0)).build()))
                        .build());

        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(ScheduleAppointmentRequest.builder()
                        .patientId(10L)
                        .doctorId(1L)
                        .slot(slot)
                        .build())
        );

        assertEquals("SLOT_NOT_AVAILABLE", ex.getCode());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldSaveWhenSlotComesFromConfiguredAvailability() {
        LocalDateTime slot = LocalDate.of(2026, 9, 21).atTime(8, 0);
        when(doctorQueryPort.findById(1L))
                .thenReturn(Optional.of(new DoctorSummary(1L, "Dra. Maria Lopez", "Medicina General")));
        when(queryAvailableSlotsUseCase.execute(1L, slot.toLocalDate()))
                .thenReturn(AvailableSlotsResultDTO.builder()
                        .slots(List.of(TimeSlotDTO.builder().start(slot).build()))
                        .build());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment saved = useCase.execute(ScheduleAppointmentRequest.builder()
                .patientId(10L)
                .doctorId(1L)
                .slot(slot)
                .build());

        assertEquals(slot, saved.getAppointmentDate());
        verify(appointmentRepository).save(any(Appointment.class));
    }
}
