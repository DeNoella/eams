package com.eams.controller;

import com.eams.dto.request.LicenceCreateDto;
import com.eams.dto.response.*;
import com.eams.service.LicenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/licences")
@RequiredArgsConstructor
public class LicenceController {
    private final LicenceService licenceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<PageResponse<LicenceResponseDto>>> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LicenceResponseDto> result = licenceService.getAllLicences(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<LicenceResponseDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(licenceService.getLicenceById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<LicenceResponseDto>> create(@Valid @RequestBody LicenceCreateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(licenceService.createLicence(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<LicenceResponseDto>> update(@PathVariable UUID id, @Valid @RequestBody LicenceCreateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(licenceService.updateLicence(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {
        licenceService.deleteLicence(id);
        return ResponseEntity.ok(ApiResponse.success("Licence deleted"));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<PageResponse<LicenceResponseDto>>> expiring(
        @RequestParam(defaultValue = "30") int days,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size) {
        Page<LicenceResponseDto> result = licenceService.getExpiringWithin(days, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ByteArrayResource> export(
        @RequestParam(defaultValue = "xlsx") String format,
        @RequestParam(required = false) Integer expiringWithinDays) {
        LicenceService.ExportFile file = licenceService.exportLicences(format, expiringWithinDays);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
            .contentType(MediaType.parseMediaType(file.contentType()))
            .contentLength(file.content().length)
            .body(new ByteArrayResource(file.content()));
    }

    @GetMapping("/compliance-report")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> report(@RequestParam UUID organisationId) {
        return ResponseEntity.ok(ApiResponse.success(licenceService.getComplianceReport(organisationId)));
    }
}
