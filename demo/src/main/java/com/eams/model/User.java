package com.eams.model;

import com.eams.enums.EmploymentStatus;
import com.eams.enums.EmploymentStatus;
import com.eams.model.base.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.eams.enums.EmploymentType;

@Entity
@Table(name = "users")
@Where(clause = "is_deleted = false")
@Getter
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "employee_id", nullable = false, length = 50)
    private String employeeId;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_manager_id")
    private User lineManager;

    @Column(name = "ad_username", length = 255)
    private String adUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 30)
    @Builder.Default
    private EmploymentType employmentType = EmploymentType.PERMANENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", length = 30)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(name = "access_expiry_date")
    private LocalDate accessExpiryDate;

    @Column(name = "mfa_enabled")
    private Boolean mfaEnabled = true;

    @Column(name = "mfa_method", length = 30)
    private String mfaMethod = "MAGIC_LINK";

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "failed_login_count")
    private Short failedLoginCount = 0;

    @Column(name = "locked_at")
    private OffsetDateTime lockedAt;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;
}