package com.eams.controller;

import com.eams.dto.request.CertificateCreateDto;
import com.eams.dto.response.*;
import com.eams.enums.CertificateStatus;
import com.eams.service.CertificateService;
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
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateController {
    private final CertificateService certificateService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<PageResponse<CertificateResponseDto>>> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size,
        @RequestParam(required = false) CertificateStatus status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CertificateResponseDto> result = certificateService.getAllCertificates(pageable, status);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<CertificateResponseDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.getCertificateById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<CertificateResponseDto>> create(@Valid @RequestBody CertificateCreateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.createCertificate(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CertificateResponseDto>> update(@PathVariable UUID id, @Valid @RequestBody CertificateCreateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.updateCertificate(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {
        certificateService.deleteCertificate(id);
        return ResponseEntity.ok(ApiResponse.success("Certificate deleted"));
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<CertificateResponseDto>> renew(@PathVariable UUID id, @RequestParam UUID renewedByUserId) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.renewCertificate(id, renewedByUserId)));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<PageResponse<CertificateResponseDto>>> expiring(
        @RequestParam(defaultValue = "30") int days,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size) {
        Page<CertificateResponseDto> result = certificateService.getExpiringWithin(days, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(result)));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ByteArrayResource> export(
        @RequestParam(defaultValue = "xlsx") String format,
        @RequestParam(required = false) CertificateStatus status,
        @RequestParam(required = false) Integer expiringWithinDays) {
        CertificateService.ExportFile file = certificateService.exportCertificates(format, status, expiringWithinDays);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
            .contentType(MediaType.parseMediaType(file.contentType()))
            .contentLength(file.content().length)
            .body(new ByteArrayResource(file.content()));
    }

    @GetMapping("/expiry-calendar")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, java.util.List<CertificateResponseDto>>>> expiryCalendar(
        @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.getExpiryCalendar(months)));
    }
}
