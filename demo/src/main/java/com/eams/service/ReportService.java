package com.eams.service;

import com.eams.dto.response.DashboardResponseDto;
import com.eams.exception.ResourceNotFoundException;
import com.eams.model.Asset;
import com.eams.model.Certificate;
import com.eams.model.Licence;
import com.eams.repository.AssetRepository;
import com.eams.repository.AuditLogRepository;
import com.eams.repository.CertificateRepository;
import com.eams.repository.LicenceRepository;
import com.eams.repository.UserRepository;
import com.eams.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final AssetRepository assetRepository;
    private final CertificateRepository certificateRepository;
    private final LicenceRepository licenceRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final DashboardService dashboardService;

    public Map<String, Object> generateAssetInventoryReport(String format) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        List<Asset> assets = assetRepository.findAllByOrganisationIdAndIsDeletedFalse(orgId, Pageable.unpaged()).getContent();
        Map<String, Long> byStatus = assets.stream()
            .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()));
        Map<String, Long> byCategory = assets.stream()
            .collect(Collectors.groupingBy(
                a -> a.getAssetCategory() != null ? a.getAssetCategory().getName() : "Uncategorised",
                Collectors.counting()));
        Map<String, Object> body = new HashMap<>();
        body.put("report", "asset-inventory");
        body.put("format", format);
        body.put("generatedAt", LocalDate.now().toString());
        body.put("organisationId", orgId.toString());
        body.put("totalAssets", assets.size());
        body.put("assetCountByStatus", byStatus);
        body.put("assetCountByCategory", byCategory);
        body.put("executive", dashboardService.getExecutiveDashboard());
        return body;
    }

    public Map<String, Object> generateLicenceComplianceReport(UUID organisationId) {
        UUID orgId = resolveOrg(organisationId);
        List<Licence> all = licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId);
        long overAllocated = all.stream()
            .filter(l -> l.getTotalSeats() != null && l.getUsedSeats() != null && l.getUsedSeats() > l.getTotalSeats())
            .count();
        LocalDate soon = LocalDate.now().plusDays(90);
        long expiringSoon = all.stream().filter(l -> l.getExpiryDate() != null && !l.getExpiryDate().isAfter(soon)).count();
        Map<String, Object> body = new HashMap<>();
        body.put("report", "licence-compliance");
        body.put("organisationId", orgId.toString());
        body.put("generatedAt", LocalDate.now().toString());
        body.put("totalLicences", all.size());
        body.put("overAllocatedSeatCount", overAllocated);
        body.put("expiringWithin90Days", expiringSoon);
        body.put("detail", licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId, Pageable.ofSize(500)).getContent().stream()
            .map(l -> Map.of(
                "id", l.getId().toString(),
                "asset", l.getAsset() != null ? l.getAsset().getName() : "",
                "type", l.getLicenceType().name(),
                "expiryDate", l.getExpiryDate() != null ? l.getExpiryDate().toString() : "",
                "status", l.getStatus().name(),
                "utilisationPct", l.getUtilisationPct() != null ? l.getUtilisationPct() : 0))
            .toList());
        return body;
    }

    public Map<String, Object> generateItAuditReport(UUID organisationId, LocalDate from, LocalDate to) {
        UUID orgId = resolveOrg(organisationId);
        OffsetDateTime start = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).minusNanos(1);
        long events = auditLogRepository.countByOrganisation_IdAndEventTimestampBetween(orgId, start, end);
        Map<String, Object> body = new HashMap<>();
        body.put("report", "it-audit");
        body.put("organisationId", orgId.toString());
        body.put("from", from.toString());
        body.put("to", to.toString());
        body.put("auditEventCount", events);
        body.put("generatedAt", LocalDate.now().toString());
        return body;
    }

    public Map<String, Object> generateSecurityPostureReport(UUID organisationId) {
        UUID orgId = resolveOrg(organisationId);
        LocalDate d30 = LocalDate.now().plusDays(30);
        List<Certificate> certs = certificateRepository.findByAsset_Organisation_IdAndExpiryDateBeforeAndIsDeletedFalse(orgId, d30);
        DashboardResponseDto dash = dashboardService.getExecutiveDashboard();
        Map<String, Object> body = new HashMap<>();
        body.put("report", "security-posture");
        body.put("organisationId", orgId.toString());
        body.put("generatedAt", LocalDate.now().toString());
        body.put("certificatesExpiring30Days", dash.getCertificatesExpiring30Days());
        body.put("certificatesExpiringSoonCount", certs.size());
        body.put("licenceSeatAlerts", dash.getLicencesAboveSeatThreshold());
        body.put("totalUsers", userRepository.countByOrganisation_Id(orgId));
        return body;
    }

    private UUID resolveOrg(UUID organisationId) {
        UUID current = TenantContextHolder.getCurrentTenantId();
        if (organisationId != null && !organisationId.equals(current)) {
            throw new ResourceNotFoundException("Organisation scope mismatch");
        }
        return current;
    }
}
