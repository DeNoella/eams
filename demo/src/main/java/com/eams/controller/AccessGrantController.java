package com.eams.controller;

import com.eams.dto.request.AccessGrantCreateDto;
import com.eams.dto.response.*;
import com.eams.service.AccessGrantService;
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
@RequestMapping("/api/v1/access-grants")
@RequiredArgsConstructor
public class AccessGrantController {
    private final AccessGrantService accessGrantService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<AccessGrantResponseDto>>> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AccessGrantResponseDto> result = accessGrantService.getAllGrants(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AccessGrantResponseDto>> create(@Valid @RequestBody AccessGrantCreateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(accessGrantService.createAccessGrant(dto)));
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AccessGrantResponseDto>> revoke(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        UUID revokingUserId = UUID.fromString(body.get("revokingUserId"));
        return ResponseEntity.ok(ApiResponse.success(accessGrantService.revokeAccessGrant(id, revokingUserId, body.get("confirmation"))));
    }

    @GetMapping("/overdue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AccessGrantResponseDto>>> overdue() {
        return ResponseEntity.ok(ApiResponse.success(accessGrantService.getOverdueGrants()));
    }
}
