package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.export.AppointmentCsvWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Exporta el listado de una jornada (médico o terapista + fecha) a CSV.
 */
@Service
@RequiredArgsConstructor
public class ExportAppointmentsCsvUseCase {

    private final SearchAppointmentsByDoctorAndDateUseCase searchUseCase;
    private final AppointmentCsvWriter csvWriter;

    @Transactional(readOnly = true)
    public byte[] execute(Long doctorId, LocalDate date) {
        return csvWriter.write(searchUseCase.execute(doctorId, date));
    }
}
