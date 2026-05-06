package com.eams.model;

import com.eams.enums.*;
import com.eams.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "certificates")
@Where(clause = "is_deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Certificate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", nullable = false, length = 50)
    private CertificateType certificateType;

    @Column(name = "issuing_authority", nullable = false, length = 255)
    private String issuingAuthority;

    @Column(name = "subject_dn", nullable = false)
    private String subjectDn;

    @Column(name = "common_name", nullable = false, length = 255)
    private String commonName;

    @Column(name = "san_entries", columnDefinition = "text[]")
    private String[] sanEntries;

    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "fingerprint_sha256", nullable = false, length = 64)
    private String fingerprintSha256;

    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm", nullable = false, length = 30)
    private CertificateAlgorithm algorithm;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "renewal_lead_days")
    private Integer renewalLeadDays = 90;

    @Column(name = "auto_renewal_enabled")
    private Boolean autoRenewalEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 20)
    private CertificateEnvironment environment = CertificateEnvironment.PRODUCTION;

    @Column(name = "key_store_location")
    private String keyStoreLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_cert_id")
    private Certificate previousCert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renewed_by_user_id")
    private User renewedBy;

    @Column(name = "renewed_at")
    private OffsetDateTime renewedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CertificateStatus status = CertificateStatus.VALID;

    // Computed daily — not stored in DB, calculated on retrieval
    @Transient
    public long getDaysRemaining() {
        if (expiryDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
}