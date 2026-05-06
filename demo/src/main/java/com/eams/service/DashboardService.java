package com.eams.service;

import com.eams.dto.response.DashboardResponseDto;
import com.eams.model.Asset;
import com.eams.repository.AccessGrantRepository;
import com.eams.repository.AssetRepository;
import com.eams.repository.CertificateRepository;
import com.eams.repository.ChangeRequestRepository;
import com.eams.repository.LicenceRepository;
import com.eams.repository.UserRepository;
import com.eams.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    private final AssetRepository assetRepository;
    private final CertificateRepository certificateRepository;
    private final LicenceRepository licenceRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final AccessGrantRepository accessGrantRepository;
    private final UserRepository userRepository;

    public DashboardResponseDto getExecutiveDashboard() {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        var assets = assetRepository.findAllByOrganisationIdAndIsDeletedFalse(orgId, org.springframework.data.domain.Pageable.unpaged()).getContent();

        Map<String, Long> byStatus = assets.stream().collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()));
        Map<String, Long> byCriticality = assets.stream().collect(Collectors.groupingBy(a -> a.getCriticality().name(), Collectors.counting()));
        Map<String, Long> byCategory = assets.stream().collect(Collectors.groupingBy(
            a -> a.getAssetCategory() != null ? a.getAssetCategory().getName() : "Uncategorised",
            Collectors.counting()));
        long userCount = userRepository.countByOrganisation_Id(orgId);
        long cert30 = certificateRepository.findByAsset_Organisation_IdAndExpiryDateBeforeAndIsDeletedFalse(orgId, LocalDate.now().plusDays(30)).size();
        long cert90 = certificateRepository.findByAsset_Organisation_IdAndExpiryDateBeforeAndIsDeletedFalse(orgId, LocalDate.now().plusDays(90)).size();
        long cert180 = certificateRepository.findByAsset_Organisation_IdAndExpiryDateBeforeAndIsDeletedFalse(orgId, LocalDate.now().plusDays(180)).size();
        long licenceThreshold = licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId).stream()
            .filter(l -> l.getUtilisationPct() >= (l.getSeatAlertThresholdPct() == null ? 85 : l.getSeatAlertThresholdPct())).count();
        long overdueReverts = changeRequestRepository
            .findByOrganisationIdAndStatusAndScheduledRevertDateBeforeAndIsDeletedFalse(
                orgId, com.eams.enums.ChangeStatus.PENDING_REVERT, LocalDate.now())
            .size();
        long grantsSoon = accessGrantRepository
            .findByOrganisationIdAndStatusAndExpiresAtBeforeAndIsDeletedFalse(
                orgId, com.eams.enums.AccessGrantStatus.ACTIVE, OffsetDateTime.now().plusDays(7))
            .size();

        return DashboardResponseDto.builder()
            .totalAssets(assets.size())
            .totalUsers(userCount)
            .assetCountByStatus(byStatus)
            .assetCountByCriticality(byCriticality)
            .assetCountByCategory(byCategory)
            .certificatesExpiring30Days(cert30)
            .certificatesExpiring90Days(cert90)
            .certificatesExpiring180Days(cert180)
            .licencesAboveSeatThreshold(licenceThreshold)
            .overdueRevertsCount(overdueReverts)
            .accessGrantsExpiringSoon(grantsSoon)
            .openAiAnomaliesCount(0)
            .build();
    }

    public Map<String, Object> getAiInsights() {
        DashboardResponseDto d = getExecutiveDashboard();
        return Map.of(
            "openAiAnomaliesCount", d.getOpenAiAnomaliesCount(),
            "totalAssets", d.getTotalAssets(),
            "totalUsers", d.getTotalUsers(),
            "certificatesExpiring30Days", d.getCertificatesExpiring30Days(),
            "licencesAboveSeatThreshold", d.getLicencesAboveSeatThreshold(),
            "note", "Figures mirror the executive dashboard (live data)."
        );
    }
}
