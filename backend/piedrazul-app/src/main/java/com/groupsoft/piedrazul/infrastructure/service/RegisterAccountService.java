package com.groupsoft.piedrazul.infrastructure.service;

import com.groupsoft.piedrazul.availability.domain.model.Doctor;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorRepository;
import com.groupsoft.piedrazul.user.application.dto.RegisterUserRequest;
import com.groupsoft.piedrazul.user.application.usecase.RegisterUserUseCase;
import com.groupsoft.piedrazul.user.application.validation.RegistrationRules;
import com.groupsoft.piedrazul.user.domain.model.Role;
import com.groupsoft.piedrazul.user.domain.model.User;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterAccountService {

    private final RegisterUserUseCase registerUserUseCase;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional
    public User register(RegisterUserRequest request) {
        User user = registerUserUseCase.execute(request);
        if (user.getRole() != Role.DOCTOR) {
            return user;
        }

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .fullName(user.getFullName())
                .specialty(RegistrationRules.normalizeName(request.specialty()))
                .active(true)
                .build());
        user.setDoctorId(doctor.getId());
        return userRepository.save(user);
    }
}
