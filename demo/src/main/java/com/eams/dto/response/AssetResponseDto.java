package com.eams.dto.response;

import com.eams.enums.AssetCriticality;
import com.eams.enums.AssetStatus;
import com.eams.enums.LifecycleStage;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Data
@Builder
public class AssetResponseDto {
    private UUID id;
    private UUID assetCategoryId;
    private String assetIdDisplay;
    private String name;
    private String description;
    private String categoryName;
    private String categoryCode;
    private String versionNo;
    private LocalDate versionDate;
    private Integer numberOfLicences;
    private String hostingInstitution;
    private String functionsOfSystem;
    private String staffInCharge;
    private String serverType;
    private String operatingSystemName;
    private String osVendorName;
    private String osVersionNo;
    private String modelNumber;
    private String equipmentLicenceType;
    private UUID locationId;
    private String locationName;
    private UUID departmentId;
    private String departmentName;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID technicalOwnerId;
    private String technicalOwnerName;
    private UUID businessOwnerId;
    private String businessOwnerName;
    private AssetCriticality criticality;
    private AssetStatus status;
    private LifecycleStage lifecycleStage;
    private Short yearOfInstallation;
    private BigDecimal acquisitionCost;
    private String currencyCode;
    private BigDecimal annualCost;
    private String vendorName;
    private String purchaseOrderRef;
    private String supportContractRef;
    private LocalDate warrantyExpiryDate;
    private LocalDate supportExpiryDate;
    private LocalDate lastAuditDate;
    private String barcode;
    private String notes;
    private Long daysUntilWarrantyExpiry;
    private Long daysUntilSupportExpiry;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static long daysUntil(LocalDate date) {
        return date == null ? 0 : ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
}
