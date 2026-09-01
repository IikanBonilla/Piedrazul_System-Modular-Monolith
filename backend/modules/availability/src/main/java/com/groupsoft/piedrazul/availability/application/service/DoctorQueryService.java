package com.groupsoft.piedrazul.availability.application.service;

import com.groupsoft.piedrazul.availability.application.dto.DoctorResponseDTO;
import com.groupsoft.piedrazul.availability.domain.model.Doctor;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorQueryService {

    private final DoctorRepository doctorRepository;

    public List<DoctorResponseDTO> findAllActive() {
        return doctorRepository.findAll().stream()
                .filter(Doctor::isActive)
                .map(this::toDto)
                .toList();
    }

    private DoctorResponseDTO toDto(Doctor doctor) {
        return DoctorResponseDTO.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .specialty(doctor.getSpecialty())
                .active(doctor.isActive())
                .build();
    }
}
