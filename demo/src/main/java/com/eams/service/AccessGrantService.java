package com.eams.service;

import com.eams.dto.request.AccessGrantCreateDto;
import com.eams.dto.response.AccessGrantResponseDto;
import com.eams.enums.AccessGrantStatus;
import com.eams.exception.ResourceNotFoundException;
import com.eams.model.*;
import com.eams.repository.*;
import com.eams.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AccessGrantService {
    private final AccessGrantRepository accessGrantRepository;
    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public AccessGrantResponseDto createAccessGrant(AccessGrantCreateDto dto) {
        if (dto.getApprovingUserId().equals(dto.getRequestorUserId())) {
            throw new IllegalArgumentException("Approver cannot be the requestor");
        }
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        Organisation org = organisationRepository.findById(orgId).orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));
        AccessGrant grant = new AccessGrant();
        grant.setOrganisation(org);
        grant.setAccessType(dto.getAccessType());
        grant.setRequestor(userRepository.findById(dto.getRequestorUserId()).orElseThrow(() -> new ResourceNotFoundException("Requestor not found")));
        grant.setApprover(userRepository.findById(dto.getApprovingUserId()).orElseThrow(() -> new ResourceNotFoundException("Approver not found")));
        if (dto.getTargetAssetId() != null) {
            grant.setTargetAsset(assetRepository.findById(dto.getTargetAssetId()).orElse(null));
        }
        grant.setBusinessJustification(dto.getBusinessJustification());
        grant.setTicketReference(dto.getTicketReference());
        grant.setRiskLevel(dto.getRiskLevel());
        grant.setGrantedAt(OffsetDateTime.now());
        grant.setExpiresAt(dto.getExpiresAt());
        grant.setStatus(AccessGrantStatus.ACTIVE);
        AccessGrant saved = accessGrantRepository.save(grant);
        auditLogService.log(AuditLogService.AuditLogEntry.create(TenantContextHolder.getCurrentUserId(), "System", "access_grant", saved.getId(), saved.getAccessType().name()));
        return toDto(saved);
    }

    public AccessGrantResponseDto revokeAccessGrant(UUID grantId, UUID revokingUserId, String confirmation) {
        AccessGrant grant = requireGrant(grantId);
        if (grant.getRequestor() != null && revokingUserId.equals(grant.getRequestor().getId())) {
            throw new IllegalArgumentException("User cannot revoke own grant");
        }
        grant.setRevokedAt(OffsetDateTime.now());
        grant.setRevokedBy(userRepository.findById(revokingUserId).orElse(null));
        grant.setRevocationConfirmation(confirmation);
        grant.setStatus(AccessGrantStatus.REVOKED);
        return toDto(accessGrantRepository.save(grant));
    }

    @Transactional(readOnly = true)
    public List<AccessGrantResponseDto> getOverdueGrants() {
        return accessGrantRepository.findByStatusAndExpiresAtBefore(AccessGrantStatus.ACTIVE, OffsetDateTime.now())
            .stream().map(this::toDto).toList();
    }

    public void escalateOverdueGrants() {
        List<AccessGrant> overdue = accessGrantRepository.findByStatusAndExpiresAtBefore(AccessGrantStatus.ACTIVE, OffsetDateTime.now());
        for (AccessGrant grant : overdue) {
            grant.setStatus(AccessGrantStatus.OVERDUE);
            grant.setOverdueEscalatedAt(OffsetDateTime.now());
            if (grant.getApprover() != null) {
                emailService.sendOverdueAlert(grant.getApprover().getEmail(), grant.getApprover().getFullName(), "Access Grant Overdue", grant.getId().toString());
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<AccessGrantResponseDto> getAllGrants(Pageable pageable) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        return accessGrantRepository.findByOrganisationIdAndIsDeletedFalse(orgId, pageable).map(this::toDto);
    }

    private AccessGrant requireGrant(UUID id) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        return accessGrantRepository.findByIdAndOrganisationIdAndIsDeletedFalse(id, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Access grant not found: " + id));
    }

    private AccessGrantResponseDto toDto(AccessGrant grant) {
        return AccessGrantResponseDto.builder()
            .id(grant.getId())
            .accessType(grant.getAccessType())
            .targetAssetId(grant.getTargetAsset() != null ? grant.getTargetAsset().getId() : null)
            .requestorName(grant.getRequestor() != null ? grant.getRequestor().getFullName() : null)
            .approverName(grant.getApprover() != null ? grant.getApprover().getFullName() : null)
            .businessJustification(grant.getBusinessJustification())
            .ticketReference(grant.getTicketReference())
            .riskLevel(grant.getRiskLevel())
            .grantedAt(grant.getGrantedAt())
            .expiresAt(grant.getExpiresAt())
            .revokedAt(grant.getRevokedAt())
            .status(grant.getStatus())
            .isOverdue(grant.getExpiresAt() != null && grant.getRevokedAt() == null && grant.getExpiresAt().isBefore(OffsetDateTime.now()))
            .build();
    }
}
