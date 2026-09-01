package com.groupsoft.piedrazul.shared.port;

import com.groupsoft.piedrazul.shared.dto.UserSummary;

import java.util.Optional;

/**
 * ISP: el modulo de citas solo necesita consultar datos basicos del paciente.
 */
public interface UserQueryPort {

    Optional<UserSummary> findById(Long userId);
}
