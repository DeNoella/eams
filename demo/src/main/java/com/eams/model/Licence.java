package com.eams.model;

import com.eams.enums.LicenceStatus;
import com.eams.enums.LicenceType;
import com.eams.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@Table(name = "licences")
@Where(clause = "is_deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Licence extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "licence_type", nullable = false, length = 30)
    private LicenceType licenceType;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Column(name = "used_seats")
    private Integer usedSeats = 0;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LicenceStatus status = LicenceStatus.VALID;

    @Column(name = "renewal_lead_days")
    private Integer renewalLeadDays = 90;

    @Column(name = "software_assurance_expiry")
    private LocalDate softwareAssuranceExpiry;

    @Column(name = "key_or_reference")
    private String keyOrReference;

    @Column(name = "seat_alert_threshold_pct")
    private Short seatAlertThresholdPct = 85;

    @Transient
    public Integer getAvailableSeats() {
        if (totalSeats == null || usedSeats == null) return null;
        return totalSeats - usedSeats;
    }

    @Transient
    public Double getUtilisationPct() {
        if (totalSeats == null || totalSeats == 0 || usedSeats == null) return 0.0;
        return (double) usedSeats / totalSeats * 100;
    }

    @Transient
    public long getDaysRemaining() {
        if (expiryDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
}