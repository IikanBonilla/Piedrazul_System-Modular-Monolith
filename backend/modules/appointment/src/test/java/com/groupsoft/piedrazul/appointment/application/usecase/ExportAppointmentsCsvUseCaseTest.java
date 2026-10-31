package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentSearchResultDTO;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSearchException;
import com.groupsoft.piedrazul.appointment.application.export.AppointmentCsvWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportAppointmentsCsvUseCaseTest {

    @Mock
    private SearchAppointmentsByDoctorAndDateUseCase searchUseCase;

    @Mock
    private AppointmentCsvWriter csvWriter;

    private ExportAppointmentsCsvUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ExportAppointmentsCsvUseCase(searchUseCase, csvWriter);
    }

    @Test
    void shouldWriteTheSearchResultAsCsv() {
        LocalDate date = LocalDate.of(2026, 9, 2);
        AppointmentSearchResultDTO result = AppointmentSearchResultDTO.builder()
                .doctorId(1L)
                .doctorName("Dra. Maria Lopez")
                .date(date)
                .total(0)
                .appointments(List.of())
                .build();
        byte[] csv = {1, 2, 3};

        when(searchUseCase.execute(1L, date)).thenReturn(result);
        when(csvWriter.write(result)).thenReturn(csv);

        assertArrayEquals(csv, useCase.execute(1L, date));
        verify(csvWriter).write(result);
    }

    @Test
    void shouldNotWriteCsvWhenSearchFails() {
        LocalDate date = LocalDate.of(2026, 9, 2);
        when(searchUseCase.execute(99L, date)).thenThrow(AppointmentSearchException.doctorNotFound(99L));

        AppointmentSearchException ex = assertThrows(AppointmentSearchException.class,
                () -> useCase.execute(99L, date));

        assertEquals("DOCTOR_NOT_FOUND", ex.getCode());
        verify(csvWriter, never()).write(any());
    }
}
