package com.groupsoft.piedrazul.availability.domain.repository;

import com.groupsoft.piedrazul.availability.domain.model.DoctorSchedulingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorSchedulingConfigRepository extends JpaRepository<DoctorSchedulingConfig, Long> {

    Optional<DoctorSchedulingConfig> findByDoctorId(Long doctorId);
}
