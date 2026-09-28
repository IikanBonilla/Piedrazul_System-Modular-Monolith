package com.groupsoft.piedrazul.infrastructure.config;

import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.infrastructure.persistence.AppointmentJpaRepository;
import com.groupsoft.piedrazul.availability.domain.model.Doctor;
import com.groupsoft.piedrazul.availability.domain.model.DoctorSchedulingConfig;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorRepository;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorSchedulingConfigRepository;
import com.groupsoft.piedrazul.user.domain.model.Role;
import com.groupsoft.piedrazul.user.domain.model.User;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

/**
 * Semilla de demostracion: catalogo HE-01, usuarios con login (Carlos) y config HE-03.
 */
@Configuration
@RequiredArgsConstructor
public class DemoDataInitializer {

    private final DoctorRepository doctorRepository;
    private final DoctorSchedulingConfigRepository schedulingConfigRepository;
    private final UserRepository userRepository;
    private final AppointmentJpaRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDemoData() {
        return args -> {
            boolean freshCatalog = doctorRepository.count() == 0;
            Doctor doctor = freshCatalog
                    ? doctorRepository.save(Doctor.builder()
                    .fullName("Dra. Maria Lopez")
                    .specialty("Medicina General")
                    .active(true)
                    .build())
                    : doctorRepository.findAll().get(0);

            seedMissingSchedulingConfigs();

            User patient = ensureUser(
                    "paciente",
                    "paciente123",
                    "Juan Perez",
                    "paciente@piedrazul.local",
                    "1234567890",
                    "3001234567",
                    LocalDate.of(1995, 4, 12),
                    Role.PATIENT,
                    null
            );
            ensureUser(
                    "admin",
                    "admin123",
                    "Administrador Piedrazul",
                    "admin@piedrazul.local",
                    "1000000001",
                    "3000000001",
                    LocalDate.of(1985, 1, 15),
                    Role.ADMINISTRATOR,
                    null
            );
            ensureUser(
                    "medico",
                    "medico123",
                    doctor.getFullName(),
                    "medico@piedrazul.local",
                    "1000000002",
                    "3000000002",
                    LocalDate.of(1982, 8, 3),
                    Role.DOCTOR,
                    doctor.getId()
            );

            if (!freshCatalog || appointmentRepository.count() > 0) {
                return;
            }

            LocalDate demoDate = LocalDate.now().plusDays(1);
            appointmentRepository.save(Appointment.builder()
                    .patientId(patient.getId())
                    .doctorId(doctor.getId())
                    .appointmentDate(demoDate.atTime(9, 0))
                    .status(AppointmentStatus.CONFIRMED)
                    .whatsappNumber("3001234567")
                    .notes("Control general")
                    .build());
            appointmentRepository.save(Appointment.builder()
                    .patientId(patient.getId())
                    .doctorId(doctor.getId())
                    .appointmentDate(demoDate.atTime(10, 30))
                    .status(AppointmentStatus.PENDING)
                    .whatsappNumber("3001234567")
                    .notes("Seguimiento")
                    .build());
        };
    }

    private void seedMissingSchedulingConfigs() {
        if (schedulingConfigRepository.count() > 0) {
            return;
        }
        doctorRepository.findAll().forEach(doctor ->
                schedulingConfigRepository.save(DoctorSchedulingConfig.defaultFor(doctor.getId())));
    }

    private User ensureUser(
            String username,
            String rawPassword,
            String fullName,
            String email,
            String documentNumber,
            String phone,
            LocalDate birthDate,
            Role role,
            Long doctorId
    ) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (user == null) {
            return userRepository.save(User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(rawPassword))
                    .fullName(fullName)
                    .email(email)
                    .documentNumber(documentNumber)
                    .phone(phone)
                    .birthDate(birthDate)
                    .role(role)
                    .active(true)
                    .doctorId(doctorId)
                    .build());
        }

        boolean changed = false;
        if (!isBcrypt(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            changed = true;
        }
        if (user.getEmail() == null) {
            user.setEmail(email);
            changed = true;
        }
        if (user.getBirthDate() == null) {
            user.setBirthDate(birthDate);
            changed = true;
        }
        if (role == Role.DOCTOR && user.getDoctorId() == null && doctorId != null) {
            user.setDoctorId(doctorId);
            changed = true;
        }
        return changed ? userRepository.save(user) : user;
    }

    private boolean isBcrypt(String value) {
        return value != null
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
