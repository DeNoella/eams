package com.eams.dto.response;

import com.eams.enums.ChangeStatus;
import com.eams.enums.ChangeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ChangeRequestResponseDto {
    private UUID id;
    private String changeReference;
    private UUID targetAssetId;
    private ChangeType changeType;
    private String description;
    private String businessJustification;
    private String revertPlan;
    private LocalDate scheduledRevertDate;
    private ChangeStatus status;
    private Long daysUntilRevert;
    private Boolean isOverdue;
}
