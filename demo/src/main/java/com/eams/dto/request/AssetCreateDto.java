package com.eams.dto.request;

import com.eams.enums.AssetCriticality;
import com.eams.enums.AssetStatus;
import com.eams.enums.LifecycleStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class AssetCreateDto {
    @NotNull
    private UUID assetCategoryId;
    @NotBlank
    private String name;
    private String description;
    private UUID locationId;
    private UUID departmentId;
    private UUID technicalOwnerId;
    private UUID businessOwnerId;
    private AssetCriticality criticality = AssetCriticality.MEDIUM;
    private AssetStatus status = AssetStatus.ACTIVE;
    private LifecycleStage lifecycleStage = LifecycleStage.IN_SERVICE;
    private Short yearOfInstallation;
    private BigDecimal acquisitionCost;
    private String currencyCode;
    private BigDecimal annualCost;
    private String purchaseOrderRef;
    private String vendorName;
    private String supportContractRef;
    private LocalDate supportExpiryDate;
    private LocalDate warrantyExpiryDate;
    private String notes;
    private String barcode;

    // Dynamic category-specific fields
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
}
