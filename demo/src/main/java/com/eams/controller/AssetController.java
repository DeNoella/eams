package com.eams.controller;

import com.eams.dto.request.AssetCreateDto;
import com.eams.dto.response.*;
import com.eams.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<PageResponse<AssetResponseDto>>> getAllAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size,
            Sort.by(Sort.Direction.fromString(direction), sort));
        Page<AssetResponseDto> assets = assetService.getAllAssets(pageable, search);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(assets)));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<byte[]> exportAssets(
            @RequestParam(defaultValue = "xlsx") String format,
            @RequestParam(required = false) String search) {
        AssetService.ExportFile file = assetService.exportAssets(format, search);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
            .contentType(MediaType.parseMediaType(file.contentType()))
            .body(file.content());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<AssetResponseDto>> getAsset(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(assetService.getAssetById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<AssetResponseDto>> createAsset(
            @Valid @RequestBody AssetCreateDto dto) {
        AssetResponseDto created = assetService.createAsset(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Asset created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<AssetResponseDto>> updateAsset(
            @PathVariable UUID id,
            @Valid @RequestBody AssetCreateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
            "Asset updated successfully", assetService.updateAsset(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteAsset(@PathVariable UUID id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok(ApiResponse.success("Asset deleted successfully"));
    }
}