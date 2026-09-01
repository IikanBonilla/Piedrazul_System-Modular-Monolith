package com.groupsoft.piedrazul.availability.infrastructure.adapter;

import com.groupsoft.piedrazul.availability.domain.repository.DoctorRepository;
import com.groupsoft.piedrazul.shared.dto.DoctorSummary;
import com.groupsoft.piedrazul.shared.port.DoctorQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DoctorQueryAdapter implements DoctorQueryPort {

    private final DoctorRepository doctorRepository;

    @Override
    public boolean existsById(Long doctorId) {
        return doctorRepository.existsById(doctorId);
    }

    @Override
    public Optional<DoctorSummary> findById(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .map(doctor -> new DoctorSummary(
                        doctor.getId(),
                        doctor.getFullName(),
                        doctor.getSpecialty()
                ));
    }
}
