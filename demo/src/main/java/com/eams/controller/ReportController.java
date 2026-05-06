package com.eams.controller;

import com.eams.dto.response.ApiResponse;
import com.eams.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/asset-inventory")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assetInventory(@RequestParam(defaultValue = "pdf") String format) {
        return ResponseEntity.ok(ApiResponse.success(reportService.generateAssetInventoryReport(format)));
    }

    @GetMapping("/licence-compliance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> licenceCompliance(@RequestParam UUID organisationId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.generateLicenceComplianceReport(organisationId)));
    }

    @GetMapping("/it-audit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> itAudit(@RequestParam UUID organisationId, @RequestParam LocalDate from, @RequestParam LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.generateItAuditReport(organisationId, from, to)));
    }

    @GetMapping("/security-posture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> securityPosture(@RequestParam UUID organisationId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.generateSecurityPostureReport(organisationId)));
    }
}
