package com.eams.dto.request;

import com.eams.enums.EmploymentStatus;
import com.eams.enums.EmploymentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserCreateDto {
    @NotBlank
    private String employeeId;
    @NotBlank
    private String fullName;
    @Email
    private String email;
    private String phone;
    private String jobTitle;
    private UUID departmentId;
    private UUID locationId;
    private UUID lineManagerId;
    private String adUsername;
    private EmploymentType employmentType = EmploymentType.PERMANENT;
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;
    private LocalDate accessExpiryDate;
    private Boolean mfaEnabled = true;
    private String mfaMethod = "MAGIC_LINK";
}
