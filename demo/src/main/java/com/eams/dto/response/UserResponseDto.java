package com.eams.dto.response;

import com.eams.enums.EmploymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserResponseDto {
    private List<String> roles;
    private UUID id;
    private String employeeId;
    private String fullName;
    private String email;
    private String phone;
    private String jobTitle;
    private EmploymentStatus employmentStatus;
    private LocalDate accessExpiryDate;
    private Boolean mfaEnabled;
}
