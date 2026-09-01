package com.groupsoft.piedrazul.user.infrastructure.adapter;

import com.groupsoft.piedrazul.shared.dto.UserSummary;
import com.groupsoft.piedrazul.shared.port.UserQueryPort;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de salida: implementa el contrato del shared-kernel usando JPA.
 * DIP: el modulo de citas depende de UserQueryPort, no de esta clase.
 */
@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final UserRepository userRepository;

    @Override
    public Optional<UserSummary> findById(Long userId) {
        return userRepository.findById(userId)
                .map(user -> new UserSummary(
                        user.getId(),
                        user.getFullName(),
                        user.getDocumentNumber()
                ));
    }
}
