package com.groupsoft.piedrazul.user.application.usecase;

import com.groupsoft.piedrazul.user.application.dto.PatientSuggestionDTO;
import com.groupsoft.piedrazul.user.domain.model.Role;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Autocompletado de pacientes activos mientras se escribe la cédula.
 */
@Service
@RequiredArgsConstructor
public class SuggestPatientsByDocumentUseCase {

    private static final int MIN_PREFIX_LENGTH = 3;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PatientSuggestionDTO> execute(String documentPrefix) {
        String prefix = digitsOnly(documentPrefix);
        if (prefix.length() < MIN_PREFIX_LENGTH) {
            return List.of();
        }
        return userRepository
                .findTop8ByRoleAndActiveTrueAndDocumentNumberStartingWithOrderByDocumentNumberAsc(
                        Role.PATIENT, prefix)
                .stream()
                .map(user -> new PatientSuggestionDTO(
                        user.getId(),
                        user.getFullName(),
                        user.getDocumentNumber()))
                .toList();
    }

    private String digitsOnly(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        if (!trimmed.chars().allMatch(Character::isDigit)) {
            return "";
        }
        return trimmed;
    }
}
