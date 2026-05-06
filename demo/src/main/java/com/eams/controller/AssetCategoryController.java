package com.eams.controller;

import com.eams.dto.response.ApiResponse;
import com.eams.model.AssetCategory;
import com.eams.repository.AssetCategoryRepository;
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

@RestController
@RequestMapping("/api/v1/asset-categories")
@RequiredArgsConstructor
public class AssetCategoryController {

    private final AssetCategoryRepository assetCategoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list() {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        List<Map<String, Object>> categories = assetCategoryRepository
            .findByOrganisationIdAndIsActiveTrueAndIsDeletedFalse(orgId)
            .stream()
            .map(this::toSummary)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    private Map<String, Object> toSummary(AssetCategory category) {
        return Map.of(
            "id", category.getId(),
            "name", category.getName(),
            "code", category.getCode(),
            "defaultCriticality", category.getDefaultCriticality(),
            "requiresLocation", category.getRequiresLocation()
        );
    }
}
