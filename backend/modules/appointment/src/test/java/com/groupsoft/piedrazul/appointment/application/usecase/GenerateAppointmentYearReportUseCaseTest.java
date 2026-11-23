package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentYearReportDTO;
import com.groupsoft.piedrazul.appointment.application.dto.DoctorYearCountDTO;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSchedulingException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateAppointmentYearReportUseCaseTest {

    @Mock
    private AppointmentRepositoryPort appointmentRepository;

    @Mock
    private DoctorQueryPort doctorQueryPort;

    private GenerateAppointmentYearReportUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateAppointmentYearReportUseCase(appointmentRepository, doctorQueryPort);
    }

    @Test
    void countsAppointmentsByMonthAndDoctorAndSkipsCancelledOnes() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        when(appointmentRepository.findByDateRange(start, start.plusYears(1))).thenReturn(List.of(
                appointment(1L, LocalDateTime.of(2026, 1, 10, 9, 0), AppointmentStatus.CONFIRMED),
                appointment(1L, LocalDateTime.of(2026, 1, 12, 11, 0), AppointmentStatus.PENDING),
                appointment(1L, LocalDateTime.of(2026, 2, 2, 8, 0), AppointmentStatus.CANCELLED),
                appointment(2L, LocalDateTime.of(2026, 3, 4, 15, 0), AppointmentStatus.COMPLETED),
                appointment(2L, LocalDateTime.of(2026, 3, 5, 16, 0), AppointmentStatus.RESCHEDULED),
                appointment(1L, LocalDateTime.of(2027, 1, 1, 9, 0), AppointmentStatus.CONFIRMED)
        ));
        when(doctorQueryPort.findById(1L)).thenReturn(Optional.of(new DoctorSummary(1L, "Dra. Maria Lopez", "Medicina")));
        when(doctorQueryPort.findById(2L)).thenReturn(Optional.empty());

        AppointmentYearReportDTO report = useCase.execute(2026);

        assertEquals(2026, report.year());
        assertEquals(3, report.total());
        assertEquals(12, report.months().size());
        assertEquals(2, report.doctors().size());

        DoctorYearCountDTO maria = report.doctors().get(0);
        assertEquals("Dra. Maria Lopez", maria.doctorName());
        assertEquals(2, maria.counts().get(0));
        assertEquals(0, maria.counts().get(1));
        assertEquals(2, maria.total());

        DoctorYearCountDTO unnamed = report.doctors().get(1);
        assertEquals("Médico 2", unnamed.doctorName());
        assertEquals(1, unnamed.counts().get(2));
        assertEquals(1, unnamed.total());
    }

    @Test
    void rejectsAYearOutsideTheSupportedRange() {
        AppointmentSchedulingException error = assertThrows(
                AppointmentSchedulingException.class,
                () -> useCase.execute(1999)
        );

        assertEquals("INVALID_YEAR", error.getCode());
        assertTrue(error.getMessage().contains("2000"));
    }

    private Appointment appointment(Long doctorId, LocalDateTime date, AppointmentStatus status) {
        return Appointment.builder()
                .doctorId(doctorId)
                .patientId(10L)
                .appointmentDate(date)
                .status(status)
                .build();
    }
}
