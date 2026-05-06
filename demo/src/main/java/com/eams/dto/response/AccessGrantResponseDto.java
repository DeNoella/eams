package com.eams.dto.response;

import com.eams.enums.AccessGrantStatus;
import com.eams.enums.AccessType;
import com.eams.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class AccessGrantResponseDto {
    private UUID id;
    private AccessType accessType;
    private UUID targetAssetId;
    private String requestorName;
    private String approverName;
    private String businessJustification;
    private String ticketReference;
    private RiskLevel riskLevel;
    private OffsetDateTime grantedAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime revokedAt;
    private AccessGrantStatus status;
    private Boolean isOverdue;
}
