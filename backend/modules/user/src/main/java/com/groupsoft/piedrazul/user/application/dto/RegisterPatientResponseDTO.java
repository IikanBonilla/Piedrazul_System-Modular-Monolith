package com.groupsoft.piedrazul.user.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterPatientResponseDTO {
    private Long id;
    private String username;
    private String fullName;
    private String documentNumber;
    private String phone;
    private String message;
}
