package com.groupsoft.piedrazul.user.application.usecase;

import com.groupsoft.piedrazul.user.application.dto.PatientSuggestionDTO;
import com.groupsoft.piedrazul.user.domain.model.Role;
import com.groupsoft.piedrazul.user.domain.model.User;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuggestPatientsByDocumentUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private SuggestPatientsByDocumentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SuggestPatientsByDocumentUseCase(userRepository);
    }

    @Test
    void shouldReturnSuggestionsForActivePatients() {
        when(userRepository.findTop8ByRoleAndActiveTrueAndDocumentNumberStartingWithOrderByDocumentNumberAsc(
                Role.PATIENT, "123"))
                .thenReturn(List.of(
                        patient(10L, "Juan Pérez", "12345678"),
                        patient(11L, "Ana Gómez", "12399999")));

        List<PatientSuggestionDTO> suggestions = useCase.execute("123");

        assertEquals(List.of(
                new PatientSuggestionDTO(10L, "Juan Pérez", "12345678"),
                new PatientSuggestionDTO(11L, "Ana Gómez", "12399999")), suggestions);
    }

    @Test
    void shouldTrimThePrefix() {
        when(userRepository.findTop8ByRoleAndActiveTrueAndDocumentNumberStartingWithOrderByDocumentNumberAsc(
                Role.PATIENT, "1234"))
                .thenReturn(List.of(patient(10L, "Juan Pérez", "12345678")));

        assertEquals(1, useCase.execute("  1234 ").size());
    }

    @Test
    void shouldReturnEmptyListWhenNoPatientMatches() {
        when(userRepository.findTop8ByRoleAndActiveTrueAndDocumentNumberStartingWithOrderByDocumentNumberAsc(
                Role.PATIENT, "999"))
                .thenReturn(List.of());

        assertTrue(useCase.execute("999").isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "1", "12", "12a", "abc", "12 34"})
    void shouldNotQueryWhenPrefixIsTooShortOrNotNumeric(String prefix) {
        assertTrue(useCase.execute(prefix).isEmpty());
        verifyNoInteractions(userRepository);
    }

    private User patient(Long id, String fullName, String documentNumber) {
        return User.builder()
                .id(id)
                .fullName(fullName)
                .documentNumber(documentNumber)
                .role(Role.PATIENT)
                .active(true)
                .build();
    }
}
