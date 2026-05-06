package com.eams.controller;

import com.eams.dto.request.CheckInDto;
import com.eams.dto.request.CheckOutDto;
import com.eams.dto.request.TransferDto;
import com.eams.dto.response.ApiResponse;
import com.eams.model.CheckInOutTransaction;
import com.eams.service.CheckInOutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CheckInOutController {
    private final CheckInOutService service;

    @PostMapping("/api/v1/transactions/checkout")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<CheckInOutTransaction>> checkout(@Valid @RequestBody CheckOutDto dto) {
        return ResponseEntity.ok(ApiResponse.success(service.checkOut(dto)));
    }

    @PostMapping("/api/v1/transactions/checkin")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<CheckInOutTransaction>> checkin(@Valid @RequestBody CheckInDto dto) {
        return ResponseEntity.ok(ApiResponse.success(service.checkIn(dto)));
    }

    @PostMapping("/api/v1/transactions/transfer")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER')")
    public ResponseEntity<ApiResponse<CheckInOutTransaction>> transfer(@Valid @RequestBody TransferDto dto) {
        return ResponseEntity.ok(ApiResponse.success(service.transfer(dto)));
    }

    @GetMapping("/api/v1/transactions")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<String>> transactions() {
        return ResponseEntity.ok(ApiResponse.success("Use /api/v1/assets/{id}/transactions for asset history"));
    }

    @GetMapping("/api/v1/transactions/overdue")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<List<CheckInOutTransaction>>> overdue() {
        return ResponseEntity.ok(ApiResponse.success(service.getOverdueReturns()));
    }

    @GetMapping("/api/v1/assets/{id}/transactions")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','ASSET_MANAGER','AUDITOR','READ_ONLY_VIEWER')")
    public ResponseEntity<ApiResponse<List<CheckInOutTransaction>>> byAsset(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.getTransactionHistory(id)));
    }
}
