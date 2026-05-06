package com.eams.model;

import com.eams.enums.CustomFieldType;
import com.eams.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "custom_fields")
@Where(clause = "is_deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomField extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_category_id", nullable = false)
    private AssetCategory assetCategory;

    @Column(name = "field_label", nullable = false, length = 150)
    private String fieldLabel;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 30)
    private CustomFieldType fieldType;

    @Column(name = "options", columnDefinition = "jsonb")
    private String options;

    @Column(name = "is_mandatory")
    private Boolean isMandatory = false;

    @Column(name = "is_searchable")
    private Boolean isSearchable = true;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "validation_regex")
    private String validationRegex;

    @Column(name = "help_text")
    private String helpText;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;
}