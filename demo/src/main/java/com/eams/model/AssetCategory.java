package com.eams.model;

import com.eams.enums.AssetCriticality;
import com.eams.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "asset_categories")
@Where(clause = "is_deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssetCategory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "description")
    private String description;

    @Column(name = "id_format", nullable = false, length = 100)
    private String idFormat = "{CODE}-{LOC}-{YYYY}-{SEQ:4}";

    @Enumerated(EnumType.STRING)
    @Column(name = "default_criticality", length = 20)
    private AssetCriticality defaultCriticality = AssetCriticality.MEDIUM;

    @Column(name = "depreciation_years")
    private Integer depreciationYears;

    @Column(name = "is_physical")
    private Boolean isPhysical = false;

    @Column(name = "requires_location")
    private Boolean requiresLocation = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}