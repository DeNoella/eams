package com.eams.dto.request;

import com.eams.enums.CertificateAlgorithm;
import com.eams.enums.CertificateEnvironment;
import com.eams.enums.CertificateStatus;
import com.eams.enums.CertificateType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CertificateCreateDto {
    @NotNull
    private UUID assetId;
    @NotNull
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
    private CertificateStatus status;
}
