package com.eams.dto.request;

import com.eams.enums.AccessType;
import com.eams.enums.RiskLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AccessGrantCreateDto {
    @NotNull
    private UUID requestorUserId;
    @NotNull
    private UUID approvingUserId;
    @NotNull
    private AccessType accessType;
    private UUID targetAssetId;
    @NotNull
    private String businessJustification;
    @NotNull
    private OffsetDateTime expiresAt;
    private RiskLevel riskLevel = RiskLevel.MEDIUM;
    private String ticketReference;
}
