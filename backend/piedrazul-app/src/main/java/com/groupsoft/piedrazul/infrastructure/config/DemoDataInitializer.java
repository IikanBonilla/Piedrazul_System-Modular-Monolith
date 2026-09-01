package com.groupsoft.piedrazul.infrastructure.config;

import com.groupsoft.piedrazul.appointment.domain.model.Appointment;
import com.groupsoft.piedrazul.appointment.domain.model.AppointmentStatus;
import com.groupsoft.piedrazul.appointment.infrastructure.persistence.AppointmentJpaRepository;
import com.groupsoft.piedrazul.availability.domain.model.Doctor;
import com.groupsoft.piedrazul.availability.domain.repository.DoctorRepository;
import com.groupsoft.piedrazul.user.domain.model.Role;
import com.groupsoft.piedrazul.user.domain.model.User;
import com.groupsoft.piedrazul.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DemoDataInitializer {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentJpaRepository appointmentRepository;

    @Bean
    CommandLineRunner seedDemoData() {
        return args -> {
            if (doctorRepository.count() > 0) {
                return;
            }

            Doctor doctor = doctorRepository.save(Doctor.builder()
                    .fullName("Dra. Maria Lopez")
                    .specialty("Medicina General")
                    .active(true)
                    .build());

            User patient = userRepository.save(User.builder()
                    .username("paciente")
                    .password("paciente123")
                    .fullName("Juan Perez")
                    .documentNumber("1234567890")
                    .phone("3001234567")
                    .role(Role.PATIENT)
                    .active(true)
                    .build());

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
}
