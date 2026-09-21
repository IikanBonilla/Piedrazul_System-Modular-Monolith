package com.groupsoft.piedrazul.user.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterPatientRequest {
    private String username;
    private String password;
    private String fullName;
    private String documentNumber;
    private String phone;
}
