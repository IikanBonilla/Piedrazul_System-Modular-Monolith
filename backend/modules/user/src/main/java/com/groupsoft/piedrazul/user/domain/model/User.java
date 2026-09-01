package com.groupsoft.piedrazul.user.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;

    @Column(unique = true)
    private String documentNumber;

    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean active;

    @Column(name = "doctor_id")
    private Long doctorId;
}
