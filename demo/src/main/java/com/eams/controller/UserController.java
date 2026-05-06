package com.eams.controller;

import com.eams.dto.request.UserCreateDto;
import com.eams.dto.request.UserUpdateDto;
import com.eams.dto.response.ApiResponse;
import com.eams.dto.response.PageResponse;
import com.eams.dto.response.UserResponseDto;
import com.eams.enums.EmploymentStatus;
import com.eams.model.Asset;
import com.eams.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(users)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody UserCreateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(userService.createUser(dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@PathVariable UUID id, @RequestBody UserUpdateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, dto)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<UserResponseDto>> changeStatus(@PathVariable UUID id, @RequestParam EmploymentStatus status) {
        return ResponseEntity.ok(ApiResponse.success(userService.changeEmploymentStatus(id, status)));
    }

    @GetMapping("/{id}/assets")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<List<Asset>>> getUserAssets(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserAssets(id)));
    }

    @GetMapping("/{id}/access-review")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<Object>> getUserAccessReview(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserAccessReview(id)));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<String>> assignRole(@PathVariable UUID id, @RequestBody Map<String, UUID> body) {
        userService.assignRole(id, body.get("roleId"), body.get("assignedByUserId"));
        return ResponseEntity.ok(ApiResponse.success("Role assigned"));
    }
}
