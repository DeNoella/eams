package com.eams.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TransferDto {
    @NotNull
    private UUID assetId;
    @NotNull
    private UUID toLocationId;
    private UUID toDepartmentId;
    @NotNull
    private UUID authorisingUserId;
    private String notes;
}
