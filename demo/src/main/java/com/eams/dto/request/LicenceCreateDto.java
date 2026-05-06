package com.eams.dto.request;

import com.eams.enums.LicenceStatus;
import com.eams.enums.LicenceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LicenceCreateDto {
    @NotNull
    private UUID assetId;
    @NotNull
    private LicenceType licenceType;
    private Integer totalSeats;
    private Integer usedSeats;
    private LocalDate expiryDate;
    private LicenceStatus status;
    private Integer renewalLeadDays;
    private LocalDate softwareAssuranceExpiry;
    private String keyOrReference;
    private Short seatAlertThresholdPct;
}
