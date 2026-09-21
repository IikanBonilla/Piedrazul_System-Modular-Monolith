package com.groupsoft.piedrazul.user.application.usecase;

import com.groupsoft.piedrazul.user.application.dto.RegisterPatientRequest;
import com.groupsoft.piedrazul.user.application.dto.RegisterPatientResponseDTO;
import com.groupsoft.piedrazul.user.application.exception.PatientRegistrationException;
import com.groupsoft.piedrazul.user.domain.model.Role;
import com.groupsoft.piedrazul.user.domain.model.User;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * HE-02 / HU-2.2: registro de paciente.
 */
@Service
@RequiredArgsConstructor
public class RegisterPatientUseCase {

    private final UserRepository userRepository;

    @Transactional
    public RegisterPatientResponseDTO execute(RegisterPatientRequest request) {
        RegisterPatientRequest safeRequest = request == null
                ? RegisterPatientRequest.builder().build()
                : request;

        List<String> missing = missingFields(safeRequest);
        if (!missing.isEmpty()) {
            throw PatientRegistrationException.incomplete(missing);
        }

        String username = safeRequest.getUsername().trim();
        String documentNumber = safeRequest.getDocumentNumber().trim();

        if (userRepository.findByUsername(username).isPresent()
                || userRepository.findByDocumentNumber(documentNumber).isPresent()) {
            throw PatientRegistrationException.alreadyExists();
        }

        User saved = userRepository.save(User.builder()
                .username(username)
                .password(safeRequest.getPassword())
                .fullName(safeRequest.getFullName().trim())
                .documentNumber(documentNumber)
                .phone(safeRequest.getPhone().trim())
                .role(Role.PATIENT)
                .active(true)
                .build());

        return RegisterPatientResponseDTO.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .fullName(saved.getFullName())
                .documentNumber(saved.getDocumentNumber())
                .phone(saved.getPhone())
                .message("Registro exitoso. El paciente ya puede agendar citas.")
                .build();
    }

    private List<String> missingFields(RegisterPatientRequest request) {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getUsername())) {
            missing.add("nombre de usuario");
        }
        if (isBlank(request.getPassword())) {
            missing.add("contrasena");
        }
        if (isBlank(request.getFullName())) {
            missing.add("nombre completo");
        }
        if (isBlank(request.getDocumentNumber())) {
            missing.add("documento");
        }
        if (isBlank(request.getPhone())) {
            missing.add("telefono");
        }
        return missing;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
