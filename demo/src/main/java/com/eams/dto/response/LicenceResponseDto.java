package com.eams.dto.response;

import com.eams.enums.LicenceStatus;
import com.eams.enums.LicenceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class LicenceResponseDto {
    private UUID id;
    private UUID assetId;
    private String assetName;
    private LicenceType licenceType;
    private Integer totalSeats;
    private Integer usedSeats;
    private Integer availableSeats;
    private Double utilisationPct;
    private LocalDate expiryDate;
    private LocalDate softwareAssuranceExpiry;
    private LicenceStatus status;
    private Integer renewalLeadDays;
    private Short seatAlertThresholdPct;
    private String keyOrReference;
    private Long daysRemaining;
}
