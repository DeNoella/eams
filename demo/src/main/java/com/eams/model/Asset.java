package com.eams.model;

import com.eams.enums.AssetCriticality;
import com.eams.enums.AssetStatus;
import com.eams.enums.LifecycleStage;
import com.eams.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "assets")
@Where(clause = "is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "asset_id_display", nullable = false, length = 60)
    private String assetIdDisplay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_category_id", nullable = false)
    private AssetCategory assetCategory;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_owner_id")
    private User technicalOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_owner_id")
    private User businessOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backup_owner_id")
    private User backupOwner;

    @Enumerated(EnumType.STRING)
    @Column(name = "criticality", nullable = false, length = 20)
    private AssetCriticality criticality = AssetCriticality.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AssetStatus status = AssetStatus.ACTIVE;

    @Column(name = "year_of_installation")
    private Short yearOfInstallation;

    @Column(name = "acquisition_cost", precision = 15, scale = 2)
    private BigDecimal acquisitionCost;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency_code", length = 3)
    private String currencyCode = "USD";

    @Column(name = "annual_cost", precision = 15, scale = 2)
    private BigDecimal annualCost;

    @Column(name = "purchase_order_ref", length = 100)
    private String purchaseOrderRef;

    @Column(name = "vendor_name", length = 255)
    private String vendorName;

    @Column(name = "support_contract_ref", length = 100)
    private String supportContractRef;

    @Column(name = "support_expiry_date")
    private LocalDate supportExpiryDate;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    @Column(name = "last_audit_date")
    private LocalDate lastAuditDate;

    @Column(name = "next_audit_due_date")
    private LocalDate nextAuditDueDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_stage", length = 50)
    @Builder.Default
    private LifecycleStage lifecycleStage = LifecycleStage.IN_SERVICE;

    @Column(name = "decommission_date")
    private LocalDate decommissionDate;

    @Column(name = "decommission_reason")
    private String decommissionReason;

    // Dynamic category-specific fields (Application / Database / Server / Equipment)
    @Column(name = "version_no", length = 60)
    private String versionNo;

    @Column(name = "version_date")
    private LocalDate versionDate;

    @Column(name = "number_of_licences")
    private Integer numberOfLicences;

    @Column(name = "hosting_institution", length = 255)
    private String hostingInstitution;

    @Column(name = "functions_of_system")
    private String functionsOfSystem;

    @Column(name = "staff_in_charge", length = 255)
    private String staffInCharge;

    @Column(name = "server_type", length = 60)
    private String serverType;

    @Column(name = "operating_system_name", length = 120)
    private String operatingSystemName;

    @Column(name = "os_vendor_name", length = 120)
    private String osVendorName;

    @Column(name = "os_version_no", length = 60)
    private String osVersionNo;

    @Column(name = "model_number", length = 120)
    private String modelNumber;

    @Column(name = "equipment_licence_type", length = 60)
    private String equipmentLicenceType;
}