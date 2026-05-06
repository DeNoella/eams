package com.eams.controller;

import com.eams.dto.response.ApiResponse;
import com.eams.model.Department;
import com.eams.model.Location;
import com.eams.repository.DepartmentRepository;
import com.eams.repository.LocationRepository;
import com.eams.repository.UserRepository;
import com.eams.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lightweight lookup endpoints powering frontend dropdowns (locations, departments, users).
 * Tenant-scoped via {@link TenantContextHolder}.
 */
@RestController
@RequestMapping("/api/v1/lookups")
@RequiredArgsConstructor
public class LookupController {

    private final LocationRepository locationRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @GetMapping("/locations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> locations() {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        List<Map<String, Object>> rows = locationRepository
            .findByOrganisationIdAndIsDeletedFalse(orgId)
            .stream()
            .map(LookupController::toLocation)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    @GetMapping("/departments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> departments() {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        List<Map<String, Object>> rows = departmentRepository
            .findByOrganisationIdAndIsDeletedFalse(orgId)
            .stream()
            .map(LookupController::toDepartment)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> users() {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        List<Map<String, Object>> rows = userRepository
            .findAllByOrganisationIdAndIsDeletedFalse(orgId, org.springframework.data.domain.Pageable.unpaged())
            .getContent()
            .stream()
            .map(u -> (Map<String, Object>) Map.<String, Object>of(
                "id", u.getId(),
                "fullName", u.getFullName(),
                "email", u.getEmail() == null ? "" : u.getEmail(),
                "jobTitle", u.getJobTitle() == null ? "" : u.getJobTitle()))
            .toList();
        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    private static Map<String, Object> toLocation(Location location) {
        return Map.of(
            "id", location.getId(),
            "name", location.getName() == null ? "" : location.getName(),
            "code", location.getCode() == null ? "" : location.getCode());
    }

    private static Map<String, Object> toDepartment(Department department) {
        return Map.of(
            "id", department.getId(),
            "name", department.getName() == null ? "" : department.getName(),
            "code", department.getCode() == null ? "" : department.getCode());
    }
}
