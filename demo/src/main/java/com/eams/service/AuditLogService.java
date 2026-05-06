package com.eams.service;

import com.eams.enums.AuditActionType;
import com.eams.model.AuditLog;
import com.eams.model.Organisation;
import com.eams.repository.AuditLogRepository;
import com.eams.repository.OrganisationRepository;
import com.eams.security.TenantContextHolder;
import com.eams.util.HmacSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final OrganisationRepository organisationRepository;
    private final HmacSigner hmacSigner;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditLogEntry entry) {
        try {
            UUID organisationId = TenantContextHolder.getCurrentTenantId();
            if (organisationId == null) return;

            Organisation organisation = organisationRepository.findById(organisationId)
                    .orElse(null);
            if (organisation == null) return;

            String signature = hmacSigner.sign(buildSignatureData(entry));

            AuditLog auditLog = AuditLog.builder()
                    .organisation(organisation)
                    .eventTimestamp(OffsetDateTime.now())
                    .actorUserId(entry.actorUserId())
                    .actorDisplayName(entry.actorDisplayName() != null ?
                        entry.actorDisplayName() : "System")
                    .actorIpAddress(entry.actorIpAddress())
                    .actorUserAgent(entry.actorUserAgent())
                    .actionType(entry.actionType())
                    .resourceType(entry.resourceType())
                    .resourceId(entry.resourceId())
                    .resourceDisplay(entry.resourceDisplay())
                    .fieldName(entry.fieldName())
                    .oldValue(entry.oldValue())
                    .newValue(entry.newValue())
                    .changeReason(entry.changeReason())
                    .signature(signature)
                    .isSensitive(entry.isSensitive() != null ? entry.isSensitive() : false)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage(), e);
        }
    }

    private String buildSignatureData(AuditLogEntry entry) {
        return String.join("|",
            String.valueOf(entry.actionType()),
            String.valueOf(entry.resourceType()),
            String.valueOf(entry.resourceId()),
            String.valueOf(entry.actorUserId()),
            OffsetDateTime.now().toString()
        );
    }

    public record AuditLogEntry(
        UUID actorUserId,
        String actorDisplayName,
        String actorIpAddress,
        String actorUserAgent,
        AuditActionType actionType,
        String resourceType,
        UUID resourceId,
        String resourceDisplay,
        String fieldName,
        String oldValue,
        String newValue,
        String changeReason,
        Boolean isSensitive
    ) {
        // Convenience factory methods
        public static AuditLogEntry create(UUID actorUserId, String actorName,
                String resourceType, UUID resourceId, String resourceDisplay) {
            return new AuditLogEntry(actorUserId, actorName, null, null,
                AuditActionType.CREATE, resourceType, resourceId, resourceDisplay,
                null, null, null, null, false);
        }

        public static AuditLogEntry update(UUID actorUserId, String actorName,
                String resourceType, UUID resourceId, String resourceDisplay,
                String fieldName, String oldValue, String newValue) {
            return new AuditLogEntry(actorUserId, actorName, null, null,
                AuditActionType.UPDATE, resourceType, resourceId, resourceDisplay,
                fieldName, oldValue, newValue, null, false);
        }

        public static AuditLogEntry delete(UUID actorUserId, String actorName,
                String resourceType, UUID resourceId, String resourceDisplay) {
            return new AuditLogEntry(actorUserId, actorName, null, null,
                AuditActionType.DELETE, resourceType, resourceId, resourceDisplay,
                null, null, null, null, false);
        }
    }
}