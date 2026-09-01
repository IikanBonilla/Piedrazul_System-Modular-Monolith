package com.groupsoft.piedrazul.availability.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorResponseDTO {
    private Long id;
    private String fullName;
    private String specialty;
    private boolean active;
}
