package com.eams.service;

import com.eams.dto.request.CheckInDto;
import com.eams.dto.request.CheckOutDto;
import com.eams.dto.request.TransferDto;
import com.eams.enums.TransactionStatus;
import com.eams.enums.TransactionType;
import com.eams.exception.ResourceNotFoundException;
import com.eams.model.*;
import com.eams.repository.*;
import com.eams.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckInOutService {
    private final CheckInOutRepository repository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final DepartmentRepository departmentRepository;
    private final OrganisationRepository organisationRepository;
    private final AuditLogService auditLogService;

    public CheckInOutTransaction checkOut(CheckOutDto dto) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        Organisation org = organisationRepository.findById(orgId).orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));
        Asset asset = assetRepository.findByIdAndOrganisationIdAndIsDeletedFalse(dto.getAssetId(), orgId).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        CheckInOutTransaction tx = CheckInOutTransaction.builder()
            .organisation(org)
            .asset(asset)
            .transactionType(TransactionType.CHECK_OUT)
            .fromUser(asset.getAssignedUser())
            .toUser(dto.getToUserId() != null ? userRepository.findById(dto.getToUserId()).orElse(null) : null)
            .fromLocation(asset.getLocation())
            .toLocation(dto.getToLocationId() != null ? locationRepository.findById(dto.getToLocationId()).orElse(null) : null)
            .fromDepartment(asset.getDepartment())
            .toDepartment(dto.getToDepartmentId() != null ? departmentRepository.findById(dto.getToDepartmentId()).orElse(null) : null)
            .transactionDate(OffsetDateTime.now())
            .expectedReturnDate(dto.getExpectedReturnDate())
            .authorisingUser(userRepository.findById(dto.getAuthorisingUserId()).orElseThrow(() -> new ResourceNotFoundException("Authorising user not found")))
            .purpose(dto.getPurpose())
            .conditionOnOut(dto.getConditionOnOut())
            .status(TransactionStatus.ACTIVE)
            .build();
        asset.setAssignedUser(tx.getToUser());
        CheckInOutTransaction savedTx = repository.save(tx);
        auditLogService.log(AuditLogService.AuditLogEntry.create(
            TenantContextHolder.getCurrentUserId(),
            "System",
            "transaction",
            savedTx.getId(),
            "checkout"));
        return savedTx;
    }

    public CheckInOutTransaction checkIn(CheckInDto dto) {
        CheckInOutTransaction tx = repository.findById(dto.getTransactionId()).orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        tx.setTransactionType(TransactionType.CHECK_IN);
        tx.setActualReturnDate(LocalDate.now());
        tx.setConditionOnIn(dto.getConditionOnIn());
        tx.setNotes(dto.getNotes());
        tx.setIncidentTicketRef(dto.getIncidentTicketRef());
        boolean isOverdue = tx.getExpectedReturnDate() != null && LocalDate.now().isAfter(tx.getExpectedReturnDate());
        tx.setStatus(isOverdue ? TransactionStatus.OVERDUE : TransactionStatus.COMPLETED);
        if (tx.getAsset() != null) tx.getAsset().setAssignedUser(tx.getFromUser());
        return repository.save(tx);
    }

    public CheckInOutTransaction transfer(TransferDto dto) {
        Asset asset = assetRepository.findById(dto.getAssetId()).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        CheckInOutTransaction tx = CheckInOutTransaction.builder()
            .organisation(asset.getOrganisation())
            .asset(asset)
            .transactionType(TransactionType.TRANSFER)
            .fromLocation(asset.getLocation())
            .toLocation(locationRepository.findById(dto.getToLocationId()).orElse(null))
            .fromDepartment(asset.getDepartment())
            .toDepartment(dto.getToDepartmentId() != null ? departmentRepository.findById(dto.getToDepartmentId()).orElse(null) : null)
            .authorisingUser(userRepository.findById(dto.getAuthorisingUserId()).orElseThrow(() -> new ResourceNotFoundException("Authorising user not found")))
            .transactionDate(OffsetDateTime.now())
            .notes(dto.getNotes())
            .status(TransactionStatus.COMPLETED)
            .build();
        asset.setLocation(tx.getToLocation());
        asset.setDepartment(tx.getToDepartment());
        return repository.save(tx);
    }

    @Transactional(readOnly = true)
    public List<CheckInOutTransaction> getOverdueReturns() {
        return repository.findByStatusAndExpectedReturnDateBefore(TransactionStatus.ACTIVE, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<CheckInOutTransaction> getTransactionHistory(UUID assetId) {
        return repository.findByAssetIdAndIsDeletedFalse(assetId).stream()
            .sorted(Comparator.comparing(CheckInOutTransaction::getTransactionDate))
            .toList();
    }

    public void flagOverdueTransactions() {
        repository.findByStatusAndExpectedReturnDateBefore(TransactionStatus.ACTIVE, LocalDate.now())
            .forEach(tx -> tx.setStatus(TransactionStatus.OVERDUE));
    }
}
