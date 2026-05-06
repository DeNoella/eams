package com.eams.service;

import com.eams.dto.request.RegisterRequestDto;
import com.eams.dto.request.UserCreateDto;
import com.eams.dto.request.UserUpdateDto;
import com.eams.dto.response.UserResponseDto;
import com.eams.enums.EmploymentStatus;
import com.eams.exception.EmailAlreadyInUseException;
import com.eams.exception.ResourceNotFoundException;
import com.eams.model.*;
import com.eams.repository.*;
import com.eams.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor @Transactional
public class UserService {
    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final DepartmentRepository departmentRepository;
    private final LocationRepository locationRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    private static final UUID DEFAULT_ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        return userRepository.findAllByOrganisationIdAndIsDeletedFalse(orgId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) { return toDto(requireUser(id)); }

    public UserResponseDto createUser(UserCreateDto dto) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        if (orgId == null) orgId = DEFAULT_ORG_ID;
        Organisation organisation = organisationRepository.findById(orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));
        User user = new User();
        user.setOrganisation(organisation);
        user.setEmployeeId(dto.getEmployeeId());
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setJobTitle(dto.getJobTitle());
        user.setEmploymentType(dto.getEmploymentType());
        user.setEmploymentStatus(dto.getEmploymentStatus());
        user.setAccessExpiryDate(dto.getAccessExpiryDate());
        user.setMfaEnabled(dto.getMfaEnabled());
        user.setMfaMethod(dto.getMfaMethod());
        user.setIsActive(true);
        if (dto.getDepartmentId() != null) user.setDepartment(departmentRepository.findById(dto.getDepartmentId()).orElse(null));
        if (dto.getLocationId() != null) user.setLocation(locationRepository.findById(dto.getLocationId()).orElse(null));
        if (dto.getLineManagerId() != null) user.setLineManager(userRepository.findById(dto.getLineManagerId()).orElse(null));
        User saved = userRepository.save(user);
        assignDefaultRole(saved);
        auditLogService.log(AuditLogService.AuditLogEntry.create(saved.getId(), saved.getFullName(), "user", saved.getId(), saved.getEmail()));
        return toDto(saved);
    }

    /**
     * Self-service Sign Up: creates a user with a BCrypt-hashed password.
     * The Sign In path is unchanged — magic-link remains the primary auth flow.
     * @throws EmailAlreadyInUseException if the email is already registered (mapped to HTTP 409)
     */
    public UserResponseDto registerWithPassword(RegisterRequestDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(email)) {
            throw new EmailAlreadyInUseException("Email already in use");
        }
        Organisation organisation = organisationRepository.findById(DEFAULT_ORG_ID)
            .orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));

        String firstName = dto.getFirstName().trim();
        String lastName  = dto.getLastName().trim();
        String fullName  = (firstName + " " + lastName).trim();
        String employeeId = "U-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        User user = new User();
        user.setOrganisation(organisation);
        user.setEmployeeId(employeeId);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setMfaEnabled(true);
        user.setMfaMethod("MAGIC_LINK");
        user.setIsActive(true);

        User saved = userRepository.save(user);
        assignDefaultRole(saved);
        auditLogService.log(AuditLogService.AuditLogEntry.create(
            saved.getId(), saved.getFullName(), "user", saved.getId(), saved.getEmail()));
        return toDto(saved);
    }

    public UserResponseDto updateUser(UUID id, UserUpdateDto dto) {
        User user = requireUser(id);
        if (dto.getFullName() != null) user.setFullName(dto.getFullName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getJobTitle() != null) user.setJobTitle(dto.getJobTitle());
        if (dto.getEmploymentStatus() != null) user.setEmploymentStatus(dto.getEmploymentStatus());
        if (dto.getAccessExpiryDate() != null) user.setAccessExpiryDate(dto.getAccessExpiryDate());
        if (dto.getMfaEnabled() != null) user.setMfaEnabled(dto.getMfaEnabled());
        User saved = userRepository.save(user);
        auditLogService.log(AuditLogService.AuditLogEntry.update(saved.getId(), saved.getFullName(), "user", saved.getId(), saved.getEmail(), "profile", "old", "updated"));
        return toDto(saved);
    }

    public UserResponseDto changeEmploymentStatus(UUID userId, EmploymentStatus status) {
        User user = requireUser(userId);
        user.setEmploymentStatus(status);
        if (status == EmploymentStatus.TERMINATED) {
            user.setIsActive(false);
            triggerOffboarding(user);
        }
        return toDto(userRepository.save(user));
    }

    public List<Asset> getUserAssets(UUID userId) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        return assetRepository.findByAssignedUserIdAndOrganisationIdAndIsDeletedFalse(userId, orgId);
    }

    public void assignRole(UUID userId, UUID roleId, UUID assignedByUserId) {
        User user = requireUser(userId);
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        User assignedBy = userRepository.findById(assignedByUserId).orElseThrow(() -> new ResourceNotFoundException("Assigning user not found"));
        UserRole userRole = UserRole.builder().user(user).role(role).assignedBy(assignedBy).assignedAt(OffsetDateTime.now()).build();
        userRoleRepository.save(userRole);
        auditLogService.log(AuditLogService.AuditLogEntry.create(assignedByUserId, assignedBy.getFullName(), "user_role", userRole.getId(), role.getCode()));
    }

    public Object getUserAccessReview(UUID userId) {
        User user = requireUser(userId);
        return java.util.Map.of(
            "userId", user.getId(),
            "email", user.getEmail(),
            "roles", userRoleRepository.findRoleCodesByUserId(user.getId()),
            "activeAssets", getUserAssets(userId).size()
        );
    }

    public void triggerOffboarding(User user) {
        List<Asset> assignedAssets = getUserAssets(user.getId());
        for (Asset asset : assignedAssets) asset.setAssignedUser(null);
        assetRepository.saveAll(assignedAssets);
    }

    private void assignDefaultRole(User user) {
        roleRepository.findByCodeAndOrganisationIdAndIsDeletedFalse("READ_ONLY_VIEWER", user.getOrganisation().getId())
            .ifPresent(role -> userRoleRepository.save(UserRole.builder().user(user).role(role).assignedBy(user).build()));
    }

    private User requireUser(UUID id) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        return userRepository.findByIdAndOrganisationIdAndIsDeletedFalse(id, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
            .roles(userRoleRepository.findRoleCodesByUserId(user.getId()))
            .id(user.getId())
            .employeeId(user.getEmployeeId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .jobTitle(user.getJobTitle())
            .employmentStatus(user.getEmploymentStatus())
            .accessExpiryDate(user.getAccessExpiryDate())
            .mfaEnabled(user.getMfaEnabled())
            .build();
    }
}
