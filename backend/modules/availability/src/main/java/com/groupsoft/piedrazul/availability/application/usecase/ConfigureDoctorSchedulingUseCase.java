package com.groupsoft.piedrazul.availability.application.usecase;

import com.groupsoft.piedrazul.availability.application.dto.DoctorSchedulingConfigDTO;
import com.groupsoft.piedrazul.availability.application.dto.TimeRangeDTO;
import com.groupsoft.piedrazul.availability.application.exception.InvalidSchedulingConfigException;
import com.groupsoft.piedrazul.availability.application.mapper.DoctorSchedulingConfigMapper;
import com.groupsoft.piedrazul.availability.domain.model.DoctorSchedulingConfig;
import com.groupsoft.piedrazul.availability.domain.model.TimeRange;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorRepository;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorSchedulingConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * HE-03 / HU-3.1 a HU-3.5: persistencia atomica de parametros de agendamiento.
 */
@Service
@RequiredArgsConstructor
public class ConfigureDoctorSchedulingUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorSchedulingConfigRepository configRepository;
    private final DoctorSchedulingConfigMapper mapper;

    @Transactional
    public DoctorSchedulingConfigDTO execute(Long doctorId, DoctorSchedulingConfigDTO request) {
        if (doctorId == null || !doctorRepository.existsById(doctorId)) {
            throw InvalidSchedulingConfigException.doctorNotFound(doctorId);
        }

        DoctorSchedulingConfigDTO payload = request == null
                ? DoctorSchedulingConfigDTO.builder().build()
                : request;

        validate(payload);

        DoctorSchedulingConfig config = configRepository.findByDoctorId(doctorId)
                .orElseGet(() -> DoctorSchedulingConfig.builder().doctorId(doctorId).build());

        config.setBookingWindowWeeks(payload.getBookingWindowWeeks());
        config.setSlotIntervalMinutes(payload.getSlotIntervalMinutes());
        config.setWorkingDays(EnumSet.copyOf(payload.getWorkingDays()));

        List<TimeRange> ranges = new ArrayList<>(mapper.toTimeRanges(payload.getTimeSlots()));
        if (config.getTimeSlots() == null) {
            config.setTimeSlots(new ArrayList<>());
        } else {
            config.getTimeSlots().clear();
        }
        config.getTimeSlots().addAll(ranges);

        DoctorSchedulingConfig saved = configRepository.save(config);
        DoctorSchedulingConfigDTO dto = mapper.toDto(saved);
        dto.setMessage("Configuracion de agendamiento guardada correctamente.");
        return dto;
    }

    private void validate(DoctorSchedulingConfigDTO payload) {
        if (payload.getBookingWindowWeeks() == null || payload.getBookingWindowWeeks() <= 0) {
            throw InvalidSchedulingConfigException.invalidBookingWindow();
        }
        Set<DayOfWeek> days = payload.getWorkingDays();
        if (days == null || days.isEmpty()) {
            throw InvalidSchedulingConfigException.noWorkingDays();
        }
        List<TimeRangeDTO> slots = payload.getTimeSlots();
        if (slots == null || slots.isEmpty()) {
            throw InvalidSchedulingConfigException.missingTimeSlots();
        }
        for (TimeRangeDTO slot : slots) {
            if (slot == null || slot.getStartTime() == null || slot.getEndTime() == null
                    || !slot.getStartTime().isBefore(slot.getEndTime())) {
                throw InvalidSchedulingConfigException.invalidTimeRange();
            }
        }
        if (payload.getSlotIntervalMinutes() == null || payload.getSlotIntervalMinutes() <= 0) {
            throw InvalidSchedulingConfigException.invalidSlotInterval();
        }
    }
}
