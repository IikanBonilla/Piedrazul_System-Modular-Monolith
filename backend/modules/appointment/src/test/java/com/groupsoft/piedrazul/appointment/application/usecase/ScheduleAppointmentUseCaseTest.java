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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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

    private static final Long SESSION_PATIENT_ID = 10L;
    private static final LocalDate DATE = LocalDate.of(2026, 9, 21);
    private static final LocalDateTime SLOT = DATE.atTime(11, 0);

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
        stubHappyPath();
        stubSave();

        var result = useCase.execute(SESSION_PATIENT_ID, validRequest(SLOT, true));

        assertEquals(99L, result.getId());
        assertEquals("Juan Perez", result.getPatientName());
        assertEquals("Dra. Maria Lopez", result.getDoctorName());
        assertEquals(SLOT, result.getAppointmentDate());
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
                () -> useCase.execute(SESSION_PATIENT_ID, validRequest(SLOT, false))
        );
        assertEquals("CONFIRMATION_REQUIRED", ex.getCode());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldRejectOccupiedSlot() {
        LocalDateTime occupied = DATE.atTime(9, 0);
        stubPatientAndDoctor();
        when(doctorSchedulePort.candidateSlots(1L, DATE)).thenReturn(List.of(occupied, SLOT));
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(Appointment.builder()
                        .doctorId(1L)
                        .appointmentDate(occupied)
                        .status(AppointmentStatus.CONFIRMED)
                        .build()));

        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(SESSION_PATIENT_ID, validRequest(occupied, true))
        );

        assertEquals("SLOT_NOT_AVAILABLE", ex.getCode());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldInformWhenThereAreNoAvailableSlots() {
        LocalDateTime slot = DATE.atTime(9, 0);
        stubPatientAndDoctor();
        when(doctorSchedulePort.candidateSlots(1L, DATE)).thenReturn(List.of(slot));
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(Appointment.builder()
                        .doctorId(1L)
                        .appointmentDate(slot)
                        .status(AppointmentStatus.PENDING)
                        .build()));

        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(SESSION_PATIENT_ID, validRequest(slot, true))
        );

        assertEquals("NO_AVAILABLE_SLOTS", ex.getCode());
    }

    @Test
    void shouldNotAllowPatientToScheduleForAnotherRegisteredPatient() {
        stubSessionPatient();

        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(SESSION_PATIENT_ID, requestFor("9876543210"))
        );

        assertEquals("PATIENT_MISMATCH", ex.getCode());
        assertFalse(ex.getMessage().contains("9876543210"));
        verify(userQueryPort, never()).findByDocumentNumber(any());
        verifyNoInteractions(doctorQueryPort, doctorSchedulePort, appointmentRepository);
    }

    @Test
    void shouldRejectForeignDocumentEvenWhenItIsNotRegistered() {
        stubSessionPatient();

        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(SESSION_PATIENT_ID, requestFor("5555555555"))
        );

        assertEquals("PATIENT_MISMATCH", ex.getCode());
        verify(userQueryPort, never()).findByDocumentNumber(any());
        verifyNoInteractions(doctorQueryPort, doctorSchedulePort, appointmentRepository);
    }

    @Test
    void shouldAcceptOwnDocumentWithSurroundingSpaces() {
        stubHappyPath();
        stubSave();

        var result = useCase.execute(SESSION_PATIENT_ID, requestFor("  1234567890 "));

        assertEquals("1234567890", result.getPatientDocument());
    }

    @Test
    void shouldUseSessionPatientWhenBodyHasNoDocument() {
        stubHappyPath();
        stubSave();

        var result = useCase.execute(SESSION_PATIENT_ID, requestFor(null));

        assertEquals("Juan Perez", result.getPatientName());
        assertEquals(SESSION_PATIENT_ID, savedAppointment().getPatientId());
    }

    @Test
    void shouldFailWhenSessionPatientDoesNotExist() {
        when(userQueryPort.findById(SESSION_PATIENT_ID)).thenReturn(Optional.empty());

        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(SESSION_PATIENT_ID, validRequest(SLOT, true))
        );

        assertEquals("PATIENT_NOT_FOUND", ex.getCode());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldAcceptNotesUpTo300Characters() {
        stubHappyPath();
        stubSave();
        String notes = "a".repeat(300);

        useCase.execute(SESSION_PATIENT_ID, withNotes(notes));

        assertEquals(notes, savedAppointment().getNotes());
    }

    @Test
    void shouldRejectNotesLongerThan300Characters() {
        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(SESSION_PATIENT_ID, withNotes("a".repeat(301)))
        );

        assertEquals("INVALID_NOTES", ex.getCode());
        verifyNoInteractions(userQueryPort, appointmentRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Dolor\nde cabeza", "\tDolor", "Dolor\u0000", "Dolor\u001B[31m", "Dolor\u007F"})
    void shouldRejectNotesWithControlCharacters(String notes) {
        AppointmentSchedulingException ex = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(SESSION_PATIENT_ID, withNotes(notes))
        );

        assertEquals("INVALID_NOTES", ex.getCode());
        verifyNoInteractions(userQueryPort, appointmentRepository);
    }

    @Test
    void shouldStoreBlankNotesAsNullAndTrimTheRest() {
        stubHappyPath();
        stubSave();

        useCase.execute(SESSION_PATIENT_ID, withNotes("   "));
        useCase.execute(SESSION_PATIENT_ID, withNotes("  Control anual  "));

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository, times(2)).save(captor.capture());
        assertNull(captor.getAllValues().get(0).getNotes());
        assertEquals("Control anual", captor.getAllValues().get(1).getNotes());
    }

    private void stubSessionPatient() {
        when(userQueryPort.findById(SESSION_PATIENT_ID))
                .thenReturn(Optional.of(new UserSummary(SESSION_PATIENT_ID, "Juan Perez", "1234567890", "3001234567")));
    }

    private void stubPatientAndDoctor() {
        stubSessionPatient();
        when(doctorQueryPort.findById(1L))
                .thenReturn(Optional.of(new DoctorSummary(1L, "Dra. Maria Lopez", "Medicina General")));
    }

    private void stubHappyPath() {
        stubPatientAndDoctor();
        when(doctorSchedulePort.candidateSlots(1L, DATE)).thenReturn(List.of(DATE.atTime(9, 0), SLOT));
        when(appointmentRepository.findByDoctorAndDateRange(eq(1L), any(), any())).thenReturn(List.of());
    }

    private void stubSave() {
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            appointment.setId(99L);
            return appointment;
        });
    }

    private Appointment savedAppointment() {
        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        return captor.getValue();
    }

    private ScheduleAppointmentRequest validRequest(LocalDateTime slot, boolean confirmed) {
        return ScheduleAppointmentRequest.builder()
                .documentNumber("1234567890")
                .doctorId(1L)
                .slot(slot)
                .confirmed(confirmed)
                .build();
    }

    private ScheduleAppointmentRequest requestFor(String documentNumber) {
        ScheduleAppointmentRequest request = validRequest(SLOT, true);
        request.setDocumentNumber(documentNumber);
        return request;
    }

    private ScheduleAppointmentRequest withNotes(String notes) {
        ScheduleAppointmentRequest request = validRequest(SLOT, true);
        request.setNotes(notes);
        return request;
    }
}
