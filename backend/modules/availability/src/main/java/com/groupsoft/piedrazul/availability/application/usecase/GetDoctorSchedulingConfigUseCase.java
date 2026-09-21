package com.groupsoft.piedrazul.availability.application.usecase;

import com.groupsoft.piedrazul.availability.application.dto.DoctorSchedulingConfigDTO;
import com.groupsoft.piedrazul.availability.application.exception.InvalidSchedulingConfigException;
import com.groupsoft.piedrazul.availability.application.mapper.DoctorSchedulingConfigMapper;
import com.groupsoft.piedrazul.availability.domain.model.DoctorSchedulingConfig;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorRepository;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorSchedulingConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HE-03: precarga del formulario de configuracion del medico.
 */
@Service
@RequiredArgsConstructor
public class GetDoctorSchedulingConfigUseCase {

    private final DoctorRepository doctorRepository;
    private final DoctorSchedulingConfigRepository configRepository;
    private final DoctorSchedulingConfigMapper mapper;

    @Transactional(readOnly = true)
    public DoctorSchedulingConfigDTO execute(Long doctorId) {
        if (doctorId == null || !doctorRepository.existsById(doctorId)) {
            throw InvalidSchedulingConfigException.doctorNotFound(doctorId);
        }

        DoctorSchedulingConfig config = configRepository.findByDoctorId(doctorId)
                .orElseGet(() -> DoctorSchedulingConfig.defaultFor(doctorId));
        return mapper.toDto(config);
    }
}
