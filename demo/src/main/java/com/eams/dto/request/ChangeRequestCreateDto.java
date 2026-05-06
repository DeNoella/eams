package com.eams.dto.request;

import com.eams.enums.ChangeType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ChangeRequestCreateDto {
    @NotNull
    private UUID targetAssetId;
    @NotNull
    private ChangeType changeType;
    @NotNull
    private String description;
    @NotNull
    private String businessJustification;
    @NotNull
    private String revertPlan;
    @NotNull
    private LocalDate scheduledRevertDate;
    private String riskAssessment;
}
