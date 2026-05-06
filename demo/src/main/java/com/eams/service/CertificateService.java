package com.eams.service;

import com.eams.dto.request.CertificateCreateDto;
import com.eams.dto.response.CertificateResponseDto;
import com.eams.enums.CertificateStatus;
import com.eams.exception.ResourceNotFoundException;
import com.eams.model.Asset;
import com.eams.model.Certificate;
import com.eams.model.User;
import com.eams.repository.AssetRepository;
import com.eams.repository.CertificateRepository;
import com.eams.repository.UserRepository;
import com.eams.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificateService {
    private final CertificateRepository certificateRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<CertificateResponseDto> getAllCertificates(Pageable pageable, CertificateStatus status) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        Page<Certificate> page = status == null
            ? certificateRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId, pageable)
            : certificateRepository.findByAsset_Organisation_IdAndStatusAndIsDeletedFalse(orgId, status, pageable);
        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public CertificateResponseDto getCertificateById(UUID id) {
        return toDto(requireCertificate(id));
    }

    public CertificateResponseDto createCertificate(CertificateCreateDto dto) {
        Certificate cert = new Certificate();
        apply(cert, dto);
        Certificate saved = certificateRepository.save(cert);
        auditLogService.log(AuditLogService.AuditLogEntry.create(
            TenantContextHolder.getCurrentUserId(), "System", "certificate", saved.getId(), saved.getCommonName()));
        return toDto(saved);
    }

    public CertificateResponseDto updateCertificate(UUID id, CertificateCreateDto dto) {
        Certificate cert = requireCertificate(id);
        String oldName = cert.getCommonName();
        apply(cert, dto);
        Certificate saved = certificateRepository.save(cert);
        auditLogService.log(AuditLogService.AuditLogEntry.update(
            TenantContextHolder.getCurrentUserId(), "System", "certificate", saved.getId(), oldName, "commonName", oldName, saved.getCommonName()));
        return toDto(saved);
    }

    public void deleteCertificate(UUID id) {
        Certificate cert = requireCertificate(id);
        String label = cert.getCommonName();
        cert.setIsDeleted(true);
        certificateRepository.save(cert);
        auditLogService.log(AuditLogService.AuditLogEntry.delete(
            TenantContextHolder.getCurrentUserId(), "System", "certificate", id, label));
    }

    public CertificateResponseDto renewCertificate(UUID id, UUID renewedByUserId) {
        Certificate old = requireCertificate(id);
        Certificate renewed = Certificate.builder()
            .asset(old.getAsset())
            .certificateType(old.getCertificateType())
            .issuingAuthority(old.getIssuingAuthority())
            .subjectDn(old.getSubjectDn())
            .commonName(old.getCommonName())
            .serialNumber(old.getSerialNumber() + "-R")
            .fingerprintSha256(old.getFingerprintSha256())
            .algorithm(old.getAlgorithm())
            .issueDate(LocalDate.now())
            .expiryDate(old.getExpiryDate().plusYears(1))
            .environment(old.getEnvironment())
            .owner(old.getOwner())
            .previousCert(old)
            .renewedBy(userRepository.findById(renewedByUserId).orElse(null))
            .renewedAt(OffsetDateTime.now())
            .status(CertificateStatus.VALID)
            .build();
        return toDto(certificateRepository.save(renewed));
    }

    @Transactional(readOnly = true)
    public Map<String, List<CertificateResponseDto>> getExpiryCalendar(int months) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        LocalDate end = LocalDate.now().plusMonths(months);
        List<Certificate> certs = certificateRepository.findByAsset_Organisation_IdAndExpiryDateBeforeAndIsDeletedFalse(orgId, end);
        Map<String, List<CertificateResponseDto>> grouped = new TreeMap<>();
        for (Certificate cert : certs) {
            String month = YearMonth.from(cert.getExpiryDate()).toString();
            grouped.computeIfAbsent(month, k -> new ArrayList<>()).add(toDto(cert));
        }
        return grouped;
    }

    @Transactional(readOnly = true)
    public Page<CertificateResponseDto> getExpiringWithin(int days, Pageable pageable) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        LocalDate horizon = LocalDate.now().plusDays(Math.max(0, days));
        Page<Certificate> page = certificateRepository
            .findByAsset_Organisation_IdAndExpiryDateLessThanEqualAndIsDeletedFalse(orgId, horizon, pageable);
        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ExportFile exportCertificates(String format, CertificateStatus status, Integer expiringWithinDays) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        List<Certificate> certs;
        if (expiringWithinDays != null) {
            LocalDate horizon = LocalDate.now().plusDays(Math.max(0, expiringWithinDays));
            certs = certificateRepository
                .findByAsset_Organisation_IdAndExpiryDateBeforeAndIsDeletedFalse(orgId, horizon.plusDays(1));
        } else if (status != null) {
            certs = certificateRepository
                .findByAsset_Organisation_IdAndStatusAndIsDeletedFalse(orgId, status, Pageable.unpaged())
                .getContent();
        } else {
            certs = certificateRepository
                .findByAsset_Organisation_IdAndIsDeletedFalse(orgId, Pageable.unpaged())
                .getContent();
        }
        String normalized = format == null ? "xlsx" : format.trim().toLowerCase();
        String dateSuffix = LocalDate.now().toString();
        return switch (normalized) {
            case "csv" -> new ExportFile(
                "goshen-certificates-" + dateSuffix + ".csv",
                "text/csv",
                buildCsv(certs));
            case "xlsx", "excel" -> new ExportFile(
                "goshen-certificates-" + dateSuffix + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildExcel(certs));
            default -> throw new IllegalArgumentException("Unsupported format: " + format + ". Use csv or xlsx.");
        };
    }

    private byte[] buildCsv(List<Certificate> certs) {
        StringBuilder b = new StringBuilder();
        b.append("Common Name,Asset,Type,Environment,Issuer,Serial,Issue Date,Expiry Date,Days Remaining,Status\n");
        for (Certificate c : certs) {
            b.append(csv(c.getCommonName())).append(',')
                .append(csv(c.getAsset() != null ? c.getAsset().getName() : "")).append(',')
                .append(csv(c.getCertificateType() != null ? c.getCertificateType().name() : "")).append(',')
                .append(csv(c.getEnvironment() != null ? c.getEnvironment().name() : "")).append(',')
                .append(csv(c.getIssuingAuthority())).append(',')
                .append(csv(c.getSerialNumber())).append(',')
                .append(csv(c.getIssueDate() != null ? c.getIssueDate().toString() : "")).append(',')
                .append(csv(c.getExpiryDate() != null ? c.getExpiryDate().toString() : "")).append(',')
                .append(c.getDaysRemaining()).append(',')
                .append(csv(c.getStatus() != null ? c.getStatus().name() : ""))
                .append('\n');
        }
        return b.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildExcel(List<Certificate> certs) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = wb.createSheet("Certificates");
            String[] headers = {
                "Common Name", "Asset", "Type", "Environment", "Issuer", "Serial",
                "Issue Date", "Expiry Date", "Days Remaining", "Status"
            };
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            int rowIx = 1;
            for (Certificate c : certs) {
                Row row = sheet.createRow(rowIx++);
                row.createCell(0).setCellValue(nvl(c.getCommonName()));
                row.createCell(1).setCellValue(c.getAsset() != null ? nvl(c.getAsset().getName()) : "");
                row.createCell(2).setCellValue(c.getCertificateType() != null ? c.getCertificateType().name() : "");
                row.createCell(3).setCellValue(c.getEnvironment() != null ? c.getEnvironment().name() : "");
                row.createCell(4).setCellValue(nvl(c.getIssuingAuthority()));
                row.createCell(5).setCellValue(nvl(c.getSerialNumber()));
                row.createCell(6).setCellValue(c.getIssueDate() != null ? c.getIssueDate().toString() : "");
                row.createCell(7).setCellValue(c.getExpiryDate() != null ? c.getExpiryDate().toString() : "");
                row.createCell(8).setCellValue(c.getDaysRemaining());
                row.createCell(9).setCellValue(c.getStatus() != null ? c.getStatus().name() : "");
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate certificate Excel export", e);
        }
    }

    private static String csv(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }

    public record ExportFile(String filename, String contentType, byte[] content) {}

    public void updateCertificateStatuses() {
        certificateRepository.findAll().forEach(cert -> {
            long days = cert.getDaysRemaining();
            CertificateStatus status = calculateStatus(days);
            if (cert.getStatus() != status) cert.setStatus(status);
        });
    }

    private CertificateStatus calculateStatus(long days) {
        if (days <= 0) return CertificateStatus.EXPIRED;
        if (days <= 30) return CertificateStatus.CRITICAL;
        if (days <= 90) return CertificateStatus.WARNING;
        if (days <= 180) return CertificateStatus.REVIEW_SOON;
        return CertificateStatus.VALID;
    }

    private Certificate requireCertificate(UUID id) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        return certificateRepository.findByIdAndAsset_Organisation_IdAndIsDeletedFalse(id, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + id));
    }

    private void apply(Certificate cert, CertificateCreateDto dto) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        Asset asset = assetRepository.findByIdAndOrganisationIdAndIsDeletedFalse(dto.getAssetId(), orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        cert.setAsset(asset);
        cert.setCertificateType(dto.getCertificateType());
        cert.setIssuingAuthority(blankToDefault(dto.getIssuingAuthority(), "Unspecified"));
        cert.setSubjectDn(blankToDefault(dto.getSubjectDn(), "CN=unspecified"));
        cert.setCommonName(blankToDefault(dto.getCommonName(), "unspecified"));
        cert.setSanEntries(dto.getSanEntries());
        cert.setSerialNumber(blankToDefault(dto.getSerialNumber(), "UNKNOWN"));
        cert.setFingerprintSha256(blankToDefault(dto.getFingerprintSha256(), "UNKNOWN"));
        cert.setAlgorithm(dto.getAlgorithm());
        cert.setIssueDate(dto.getIssueDate());
        cert.setExpiryDate(dto.getExpiryDate());
        cert.setRenewalLeadDays(dto.getRenewalLeadDays());
        cert.setAutoRenewalEnabled(dto.getAutoRenewalEnabled());
        cert.setEnvironment(dto.getEnvironment());
        cert.setKeyStoreLocation(dto.getKeyStoreLocation());
        cert.setStatus(dto.getStatus() == null ? CertificateStatus.VALID : dto.getStatus());
        if (dto.getOwnerUserId() != null) {
            User owner = userRepository.findById(dto.getOwnerUserId()).orElse(null);
            cert.setOwner(owner);
        }
    }

    private static String blankToDefault(String v, String def) {
        return v == null || v.isBlank() ? def : v;
    }

    private CertificateResponseDto toDto(Certificate c) {
        return CertificateResponseDto.builder()
            .id(c.getId())
            .assetId(c.getAsset() != null ? c.getAsset().getId() : null)
            .assetName(c.getAsset() != null ? c.getAsset().getName() : null)
            .certificateType(c.getCertificateType())
            .issuingAuthority(c.getIssuingAuthority())
            .subjectDn(c.getSubjectDn())
            .commonName(c.getCommonName())
            .sanEntries(c.getSanEntries())
            .serialNumber(c.getSerialNumber())
            .fingerprintSha256(c.getFingerprintSha256())
            .algorithm(c.getAlgorithm())
            .issueDate(c.getIssueDate())
            .expiryDate(c.getExpiryDate())
            .renewalLeadDays(c.getRenewalLeadDays())
            .autoRenewalEnabled(c.getAutoRenewalEnabled())
            .environment(c.getEnvironment())
            .keyStoreLocation(c.getKeyStoreLocation())
            .ownerUserId(c.getOwner() != null ? c.getOwner().getId() : null)
            .ownerUserName(c.getOwner() != null ? c.getOwner().getFullName() : null)
            .status(c.getStatus())
            .daysRemaining(c.getDaysRemaining())
            .build();
    }
}
