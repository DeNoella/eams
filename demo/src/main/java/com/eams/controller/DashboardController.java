package com.eams.controller;

import com.eams.dto.response.ApiResponse;
import com.eams.dto.response.DashboardResponseDto;
import com.eams.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/executive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardResponseDto>> executive() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getExecutiveDashboard()));
    }

    @GetMapping("/ai-insights")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> aiInsights() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getAiInsights()));
    }
}
