package com.eams.service;

import com.eams.dto.request.ChangeRequestCreateDto;
import com.eams.dto.response.ChangeRequestResponseDto;
import com.eams.enums.ChangeStatus;
import com.eams.exception.ResourceNotFoundException;
import com.eams.model.*;
import com.eams.repository.*;
import com.eams.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangeRequestService {
    private final ChangeRequestRepository changeRequestRepository;
    private final OrganisationRepository organisationRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ChangeRequestResponseDto proposeChange(ChangeRequestCreateDto dto) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        UUID actorId = TenantContextHolder.getCurrentUserId();
        Organisation org = organisationRepository.findById(orgId).orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));
        Asset asset = assetRepository.findByIdAndOrganisationIdAndIsDeletedFalse(dto.getTargetAssetId(), orgId).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        User proposer = userRepository.findById(actorId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        ChangeRequest cr = new ChangeRequest();
        cr.setOrganisation(org);
        cr.setChangeReference(nextReference(orgId));
        cr.setTargetAsset(asset);
        cr.setChangeType(dto.getChangeType());
        cr.setDescription(dto.getDescription());
        cr.setBusinessJustification(dto.getBusinessJustification());
        cr.setRiskAssessment(dto.getRiskAssessment());
        cr.setRevertPlan(dto.getRevertPlan());
        cr.setProposedBy(proposer);
        cr.setScheduledRevertDate(dto.getScheduledRevertDate());
        cr.setStatus(ChangeStatus.PROPOSED);
        return toDto(changeRequestRepository.save(cr));
    }

    public ChangeRequestResponseDto approveChange(UUID id, UUID approvingUserId) {
        ChangeRequest cr = requireCr(id);
        if (cr.getProposedBy() != null && approvingUserId.equals(cr.getProposedBy().getId())) {
            throw new IllegalArgumentException("Approver cannot be proposer");
        }
        cr.setApprovedBy(userRepository.findById(approvingUserId).orElseThrow(() -> new ResourceNotFoundException("Approver not found")));
        cr.setStatus(ChangeStatus.APPROVED);
        return toDto(changeRequestRepository.save(cr));
    }

    public ChangeRequestResponseDto applyChange(UUID id, UUID implementingUserId) {
        ChangeRequest cr = requireCr(id);
        cr.setImplementedBy(userRepository.findById(implementingUserId).orElse(null));
        cr.setActualAppliedAt(OffsetDateTime.now());
        cr.setStatus(ChangeStatus.PENDING_REVERT);
        return toDto(changeRequestRepository.save(cr));
    }

    public ChangeRequestResponseDto revertChange(UUID id, UUID revertingUserId) {
        ChangeRequest cr = requireCr(id);
        cr.setVerifiedBy(userRepository.findById(revertingUserId).orElse(null));
        cr.setActualRevertedAt(OffsetDateTime.now());
        cr.setStatus(ChangeStatus.REVERTED);
        return toDto(changeRequestRepository.save(cr));
    }

    public ChangeRequestResponseDto makePermanent(UUID id, String reason) {
        ChangeRequest cr = requireCr(id);
        cr.setMadePermanentReason(reason);
        cr.setStatus(ChangeStatus.MADE_PERMANENT);
        return toDto(changeRequestRepository.save(cr));
    }

    @Transactional(readOnly = true)
    public List<ChangeRequestResponseDto> getOverdueReverts() {
        return changeRequestRepository.findByStatusAndScheduledRevertDateBefore(ChangeStatus.PENDING_REVERT, LocalDate.now())
            .stream().map(this::toDto).toList();
    }

    public void flagOverdueReverts() {
        changeRequestRepository.findByStatusAndScheduledRevertDateBefore(ChangeStatus.PENDING_REVERT, LocalDate.now())
            .forEach(cr -> cr.setOverdueEscalatedAt(OffsetDateTime.now()));
    }

    @Transactional(readOnly = true)
    public Page<ChangeRequestResponseDto> getAll(Pageable pageable) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        return changeRequestRepository.findByOrganisationIdAndIsDeletedFalse(orgId, pageable).map(this::toDto);
    }

    private ChangeRequest requireCr(UUID id) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        return changeRequestRepository.findByIdAndOrganisationIdAndIsDeletedFalse(id, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Change request not found"));
    }

    private String nextReference(UUID orgId) {
        int year = Year.now().getValue();
        long seq = changeRequestRepository.findTopByOrganisationIdOrderByCreatedAtDesc(orgId)
            .map(last -> {
                String[] parts = last.getChangeReference().split("-");
                return Long.parseLong(parts[parts.length - 1]) + 1;
            })
            .orElse(1L);
        return "CHG-" + year + "-" + String.format("%04d", seq);
    }

    private ChangeRequestResponseDto toDto(ChangeRequest cr) {
        return ChangeRequestResponseDto.builder()
            .id(cr.getId())
            .changeReference(cr.getChangeReference())
            .targetAssetId(cr.getTargetAsset() != null ? cr.getTargetAsset().getId() : null)
            .changeType(cr.getChangeType())
            .description(cr.getDescription())
            .businessJustification(cr.getBusinessJustification())
            .revertPlan(cr.getRevertPlan())
            .scheduledRevertDate(cr.getScheduledRevertDate())
            .status(cr.getStatus())
            .daysUntilRevert(cr.getDaysUntilRevert())
            .isOverdue(cr.isOverdue())
            .build();
    }
}
