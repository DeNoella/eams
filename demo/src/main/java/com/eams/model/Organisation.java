package com.eams.model;

import com.eams.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "organisations")
@Where(clause = "is_deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Organisation extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", unique = true, nullable = false, length = 100)
    private String slug;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "primary_color", length = 7)
    private String primaryColor = "#1B3A6B";

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Column(name = "locale", length = 10)
    private String locale = "en";

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Column(name = "subscription_plan", length = 50)
    private String subscriptionPlan = "STANDARD";

    @Column(name = "max_assets")
    private Integer maxAssets = 10000;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "settings", columnDefinition = "jsonb")
    private String settings;
}