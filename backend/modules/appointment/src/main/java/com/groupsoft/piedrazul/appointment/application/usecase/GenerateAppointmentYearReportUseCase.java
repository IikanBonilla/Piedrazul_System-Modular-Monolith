package com.groupsoft.piedrazul.appointment.application.usecase;

import com.groupsoft.piedrazul.appointment.application.dto.AppointmentYearReportDTO;
import com.groupsoft.piedrazul.appointment.application.dto.DoctorYearCountDTO;
import com.groupsoft.piedrazul.appointment.application.exception.AppointmentSchedulingException;
import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.domain.port.AppointmentRepositoryPort;
import com.groupsoft.piedrazul.shared.port.DoctorQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GenerateAppointmentYearReportUseCase {

    static final List<String> MONTHS = List.of(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    );

    private static final int MIN_YEAR = 2000;
    private static final int MAX_YEAR = 2100;
    private static final Set<AppointmentStatus> COUNTED = EnumSet.of(
            AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.COMPLETED
    );

    private final AppointmentRepositoryPort appointmentRepository;
    private final DoctorQueryPort doctorQueryPort;

    @Transactional(readOnly = true)
    public AppointmentYearReportDTO execute(int year) {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw AppointmentSchedulingException.invalidYear();
        }

        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = start.plusYears(1);
        Map<Long, int[]> byDoctor = new LinkedHashMap<>();

        for (Appointment appointment : appointmentRepository.findByDateRange(start, end)) {
            if (appointment.getAppointmentDate() == null
                    || appointment.getAppointmentDate().isBefore(start)
                    || !appointment.getAppointmentDate().isBefore(end)
                    || !COUNTED.contains(appointment.getStatus())) {
                continue;
            }
            int[] counts = byDoctor.computeIfAbsent(appointment.getDoctorId(), id -> new int[MONTHS.size()]);
            counts[appointment.getAppointmentDate().getMonthValue() - 1]++;
        }

        List<DoctorYearCountDTO> doctors = byDoctor.entrySet().stream()
                .map(entry -> toDoctorCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DoctorYearCountDTO::doctorName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(DoctorYearCountDTO::doctorId))
                .toList();

        int total = doctors.stream().mapToInt(DoctorYearCountDTO::total).sum();
        return new AppointmentYearReportDTO(year, total, MONTHS, doctors);
    }

    private DoctorYearCountDTO toDoctorCount(Long doctorId, int[] counts) {
        List<Integer> months = new ArrayList<>(counts.length);
        int total = 0;
        for (int count : counts) {
            months.add(count);
            total += count;
        }
        return new DoctorYearCountDTO(doctorId, doctorName(doctorId), months, total);
    }

    private String doctorName(Long doctorId) {
        return doctorQueryPort.findById(doctorId)
                .map(doctor -> doctor.fullName())
                .filter(name -> name != null && !name.isBlank())
                .orElse("Médico " + doctorId);
    }
}
