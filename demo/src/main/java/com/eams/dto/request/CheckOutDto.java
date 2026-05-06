package com.eams.dto.request;

import com.eams.enums.AssetCondition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CheckOutDto {
    @NotNull
    private UUID assetId;
    private UUID toUserId;
    private UUID toLocationId;
    private UUID toDepartmentId;
    private LocalDate expectedReturnDate;
    private String purpose;
    private AssetCondition conditionOnOut;
    @NotNull
    private UUID authorisingUserId;
}
