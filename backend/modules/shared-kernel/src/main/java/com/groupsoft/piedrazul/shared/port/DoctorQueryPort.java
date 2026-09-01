package com.groupsoft.piedrazul.shared.port;

import com.groupsoft.piedrazul.shared.dto.DoctorSummary;

import java.util.Optional;

/**
 * ISP: el modulo de citas solo necesita validar y enriquecer con datos del medico.
 */
public interface DoctorQueryPort {

    boolean existsById(Long doctorId);

    Optional<DoctorSummary> findById(Long doctorId);
}
