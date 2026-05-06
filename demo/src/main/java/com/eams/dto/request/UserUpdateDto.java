package com.eams.dto.request;

import com.eams.enums.EmploymentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserUpdateDto {
    private String fullName;
    private String phone;
    private String jobTitle;
    private UUID departmentId;
    private UUID locationId;
    private UUID lineManagerId;
    private String adUsername;
    private EmploymentStatus employmentStatus;
    private LocalDate accessExpiryDate;
    private Boolean mfaEnabled;
}
