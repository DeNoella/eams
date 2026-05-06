package com.eams.dto.response;

import com.eams.enums.CertificateAlgorithm;
import com.eams.enums.CertificateEnvironment;
import com.eams.enums.CertificateStatus;
import com.eams.enums.CertificateType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class CertificateResponseDto {
    private UUID id;
    private UUID assetId;
    private String assetName;
    private CertificateType certificateType;
    private String issuingAuthority;
    private String subjectDn;
    private String commonName;
    private String[] sanEntries;
    private String serialNumber;
    private String fingerprintSha256;
    private CertificateAlgorithm algorithm;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Integer renewalLeadDays;
    private Boolean autoRenewalEnabled;
    private CertificateEnvironment environment;
    private String keyStoreLocation;
    private UUID ownerUserId;
    private String ownerUserName;
    private CertificateStatus status;
    private Long daysRemaining;
}
