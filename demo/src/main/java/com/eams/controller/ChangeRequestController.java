package com.eams.controller;

import com.eams.dto.request.ChangeRequestCreateDto;
import com.eams.dto.response.*;
import com.eams.service.ChangeRequestService;
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
@RequestMapping("/api/v1/change-requests")
@RequiredArgsConstructor
public class ChangeRequestController {
    private final ChangeRequestService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ChangeRequestResponseDto>>> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChangeRequestResponseDto> result = service.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChangeRequestResponseDto>> create(@Valid @RequestBody ChangeRequestCreateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(service.proposeChange(dto)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChangeRequestResponseDto>> approve(@PathVariable UUID id, @RequestParam UUID approvingUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.approveChange(id, approvingUserId)));
    }

    @PostMapping("/{id}/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChangeRequestResponseDto>> apply(@PathVariable UUID id, @RequestParam UUID implementingUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.applyChange(id, implementingUserId)));
    }

    @PostMapping("/{id}/revert")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChangeRequestResponseDto>> revert(@PathVariable UUID id, @RequestParam UUID revertingUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.revertChange(id, revertingUserId)));
    }

    @PostMapping("/{id}/make-permanent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChangeRequestResponseDto>> makePermanent(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(service.makePermanent(id, body.get("reason"))));
    }

    @GetMapping("/overdue-reverts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ChangeRequestResponseDto>>> overdue() {
        return ResponseEntity.ok(ApiResponse.success(service.getOverdueReverts()));
    }
}
