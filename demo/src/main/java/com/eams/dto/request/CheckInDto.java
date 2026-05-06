package com.eams.dto.request;

import com.eams.enums.AssetCondition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckInDto {
    @NotNull
    private UUID transactionId;
    private AssetCondition conditionOnIn;
    private String notes;
    private String incidentTicketRef;
}
