package com.eams.service;

import com.eams.dto.response.DashboardResponseDto;
import com.eams.model.Asset;
import com.eams.model.Certificate;
import com.eams.model.Licence;
import com.eams.repository.AssetRepository;
import com.eams.repository.CertificateRepository;
import com.eams.repository.LicenceRepository;
import com.eams.repository.UserRepository;
import com.eams.security.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Rule-based assistant: answers from tenant-scoped repository data only (no external LLM APIs).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiDataAssistantService {

    private final DashboardService dashboardService;
    private final AssetRepository assetRepository;
    private final CertificateRepository certificateRepository;
    private final LicenceRepository licenceRepository;
    private final UserRepository userRepository;

    public String answer(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return "Ask me about assets, licences, certificates, users, or dashboard metrics.";
        }
        String m = rawMessage.toLowerCase(Locale.ROOT).trim();
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        DashboardResponseDto dash = dashboardService.getExecutiveDashboard();

        if (matches(m, "how many assets", "total assets", "asset count", "registered assets")) {
            return "There are **" + dash.getTotalAssets() + "** assets registered in your organisation.";
        }
        if (matches(m, "critical asset")) {
            long n = dash.getAssetCountByCriticality() == null ? 0
                : dash.getAssetCountByCriticality().getOrDefault("CRITICAL", 0L);
            return "Assets marked **CRITICAL**: **" + n + "**.";
        }
        if (matches(m, "asset") && (m.contains("status") || m.contains("distribution") || m.contains("breakdown"))) {
            return formatMap("Asset counts by **status**", dash.getAssetCountByStatus());
        }
        if (matches(m, "asset") && (m.contains("category") || m.contains("taxonomy"))) {
            return categoryBreakdown(orgId);
        }
        if (matches(m, "certificate") && matches(m, "expir", "soon", "upcoming")) {
            return expiringCertificatesSummary(orgId);
        }
        if (matches(m, "licence", "license") && matches(m, "expir", "soon", "upcoming")) {
            return expiringLicencesSummary(orgId);
        }
        if (matches(m, "seat", "utilisation", "utilization", "overallocated", "over-allocated")) {
            long over = dash.getLicencesAboveSeatThreshold();
            List<Licence> all = licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId);
            long overSeats = all.stream()
                .filter(l -> l.getTotalSeats() != null && l.getUsedSeats() != null && l.getUsedSeats() > l.getTotalSeats())
                .count();
            return "**Licence seat alerts (at/above threshold):** " + over + ".\n"
                + "**Licences with used seats greater than purchased seats:** " + overSeats + ".";
        }
        if (matches(m, "user", "people", "staff") && matches(m, "how many", "count", "number of")) {
            long users = userRepository.countByOrganisation_Id(orgId);
            return "Your organisation has **" + users + "** user records (non-deleted).";
        }
        if (matches(m, "dashboard", "summary", "overview", "kpi")) {
            return executiveNarrative(dash);
        }
        if (matches(m, "check-in", "checkout", "check out", "revert", "change request")) {
            return "**Open operational signals:** overdue change-request reverts: **"
                + dash.getOverdueRevertsCount() + "**; access grants expiring within ~7 days: **"
                + dash.getAccessGrantsExpiringSoon() + "**.";
        }
        if (matches(m, "asset") && anyOf(m, "list", "show", "recent", "latest")) {
            return listAssets(orgId);
        }
        if ((matches(m, "certificate") || matches(m, "cert")) && anyOf(m, "list", "show", "all")) {
            return listCertificates(orgId);
        }
        if (anyOf(m, "licence", "license") && anyOf(m, "list", "show", "all")) {
            return listLicences(orgId);
        }
        if (anyOf(m, "cost", "spend", "value", "worth") && matches(m, "asset")) {
            return assetCostSummary(orgId);
        }
        if (anyOf(m, "most critical", "highest criticality", "top critical")) {
            return topCriticalAssets(orgId);
        }
        if (matches(m, "expire") && matches(m, "today") ) {
            return expiringToday(orgId);
        }
        if (anyOf(m, "find ", "search ", "lookup ", "who owns", "owner of", "where is")) {
            String term = extractSearchTerm(m);
            if (!term.isBlank()) {
                return searchEverything(orgId, term);
            }
        }
        return defaultHelp(dash);
    }

    private static boolean anyOf(String m, String... needles) {
        for (String n : needles) if (m.contains(n)) return true;
        return false;
    }

    private static String extractSearchTerm(String m) {
        for (String trigger : new String[]{"find ", "search ", "lookup ", "who owns ", "owner of ", "where is "}) {
            int idx = m.indexOf(trigger);
            if (idx >= 0) return m.substring(idx + trigger.length()).replaceAll("[?.!]", "").trim();
        }
        return "";
    }

    private String listAssets(UUID orgId) {
        List<Asset> assets = assetRepository.findAllByOrganisationIdAndIsDeletedFalse(orgId, Pageable.unpaged()).getContent();
        assets.sort(Comparator.comparing(Asset::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(assets.size()).append(" assets** registered.\n");
        int cap = Math.min(10, assets.size());
        for (int i = 0; i < cap; i++) {
            Asset a = assets.get(i);
            sb.append("\n• **").append(nullSafe(a.getName())).append("** _(")
                .append(nullSafe(a.getAssetIdDisplay())).append(")_ — ")
                .append(a.getStatus()).append(", ").append(a.getCriticality());
            if (a.getLocation() != null) sb.append(" @ ").append(a.getLocation().getName());
        }
        if (assets.size() > cap) sb.append("\n_…and ").append(assets.size() - cap).append(" more._");
        return sb.toString();
    }

    private String listCertificates(UUID orgId) {
        List<Certificate> certs = certificateRepository
            .findByAsset_Organisation_IdAndIsDeletedFalse(orgId, Pageable.unpaged())
            .getContent();
        certs.sort(Comparator.comparing(Certificate::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())));
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(certs.size()).append(" certificates** tracked.\n");
        int cap = Math.min(10, certs.size());
        for (int i = 0; i < cap; i++) {
            Certificate c = certs.get(i);
            sb.append("\n• **").append(nullSafe(c.getCommonName())).append("** — ")
                .append(c.getEnvironment()).append(", expires **").append(c.getExpiryDate())
                .append("** (").append(c.getStatus()).append(")");
        }
        if (certs.size() > cap) sb.append("\n_…and ").append(certs.size() - cap).append(" more._");
        return sb.toString();
    }

    private String listLicences(UUID orgId) {
        List<Licence> list = licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId);
        list.sort(Comparator.comparing(Licence::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())));
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(list.size()).append(" licences** tracked.\n");
        int cap = Math.min(10, list.size());
        for (int i = 0; i < cap; i++) {
            Licence l = list.get(i);
            String asset = l.getAsset() != null ? l.getAsset().getName() : "?";
            sb.append("\n• **").append(l.getLicenceType()).append("** on _").append(asset)
                .append("_ — seats ").append(l.getUsedSeats() == null ? 0 : l.getUsedSeats())
                .append('/').append(l.getTotalSeats() == null ? "∞" : l.getTotalSeats())
                .append(", expires ").append(l.getExpiryDate());
        }
        if (list.size() > cap) sb.append("\n_…and ").append(list.size() - cap).append(" more._");
        return sb.toString();
    }

    private String assetCostSummary(UUID orgId) {
        List<Asset> assets = assetRepository.findAllByOrganisationIdAndIsDeletedFalse(orgId, Pageable.unpaged()).getContent();
        Map<String, BigDecimal> totals = new HashMap<>();
        BigDecimal annualGrand = BigDecimal.ZERO;
        for (Asset a : assets) {
            String ccy = a.getCurrencyCode() == null ? "USD" : a.getCurrencyCode();
            if (a.getAcquisitionCost() != null) {
                totals.merge(ccy, a.getAcquisitionCost(), BigDecimal::add);
            }
            if (a.getAnnualCost() != null) {
                annualGrand = annualGrand.add(a.getAnnualCost());
            }
        }
        if (totals.isEmpty()) return "_No asset cost data recorded yet._";
        StringBuilder sb = new StringBuilder("**Asset value** (sum of acquisition cost):");
        totals.forEach((k, v) -> sb.append("\n• ").append(k).append(" **").append(v.toPlainString()).append("**"));
        sb.append("\n**Annual operating cost** (any currency): **").append(annualGrand.toPlainString()).append("**");
        return sb.toString();
    }

    private String topCriticalAssets(UUID orgId) {
        List<Asset> assets = assetRepository.findAllByOrganisationIdAndIsDeletedFalse(orgId, Pageable.unpaged()).getContent();
        List<Asset> critical = assets.stream()
            .filter(a -> a.getCriticality() != null && a.getCriticality().name().equals("CRITICAL"))
            .sorted(Comparator.comparing(Asset::getName, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
        if (critical.isEmpty()) return "_No CRITICAL assets right now._";
        StringBuilder sb = new StringBuilder("**").append(critical.size()).append(" CRITICAL assets:**");
        int cap = Math.min(10, critical.size());
        for (int i = 0; i < cap; i++) {
            Asset a = critical.get(i);
            sb.append("\n• **").append(nullSafe(a.getName())).append("** (").append(a.getStatus()).append(')');
        }
        if (critical.size() > cap) sb.append("\n_…and ").append(critical.size() - cap).append(" more._");
        return sb.toString();
    }

    private String expiringToday(UUID orgId) {
        LocalDate today = LocalDate.now();
        List<Certificate> certs = certificateRepository
            .findByAsset_Organisation_IdAndExpiryDateBeforeAndIsDeletedFalse(orgId, today.plusDays(1));
        certs = certs.stream().filter(c -> today.equals(c.getExpiryDate())).toList();
        List<Licence> licences = licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId).stream()
            .filter(l -> today.equals(l.getExpiryDate())).toList();
        return "**Expiring today:** " + certs.size() + " certificates, " + licences.size() + " licences.";
    }

    private String searchEverything(UUID orgId, String rawTerm) {
        String term = rawTerm.toLowerCase(Locale.ROOT);
        List<String> hits = new ArrayList<>();
        for (Asset a : assetRepository.findAllByOrganisationIdAndIsDeletedFalse(orgId, Pageable.unpaged()).getContent()) {
            if (nullSafe(a.getName()).toLowerCase(Locale.ROOT).contains(term)
                || nullSafe(a.getAssetIdDisplay()).toLowerCase(Locale.ROOT).contains(term)
                || nullSafe(a.getBarcode()).toLowerCase(Locale.ROOT).contains(term)
                || nullSafe(a.getVendorName()).toLowerCase(Locale.ROOT).contains(term)) {
                String owner = a.getTechnicalOwner() != null ? a.getTechnicalOwner().getFullName() : "—";
                String loc = a.getLocation() != null ? a.getLocation().getName() : "—";
                hits.add("• **Asset** _" + a.getName() + "_ (" + a.getAssetIdDisplay() + ") · "
                    + a.getStatus() + " · " + a.getCriticality() + " · owner: " + owner + " · location: " + loc);
            }
        }
        for (Certificate c : certificateRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId, Pageable.unpaged()).getContent()) {
            if (nullSafe(c.getCommonName()).toLowerCase(Locale.ROOT).contains(term)
                || nullSafe(c.getSerialNumber()).toLowerCase(Locale.ROOT).contains(term)) {
                hits.add("• **Certificate** _" + c.getCommonName() + "_ — " + c.getEnvironment()
                    + ", expires " + c.getExpiryDate() + " (" + c.getStatus() + ")");
            }
        }
        for (Licence l : licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId)) {
            String asset = l.getAsset() != null ? l.getAsset().getName() : "";
            if (asset.toLowerCase(Locale.ROOT).contains(term)
                || nullSafe(l.getKeyOrReference()).toLowerCase(Locale.ROOT).contains(term)) {
                hits.add("• **Licence** _" + l.getLicenceType() + "_ on " + asset
                    + " — " + l.getUsedSeats() + "/" + l.getTotalSeats() + " seats, expires " + l.getExpiryDate());
            }
        }
        if (hits.isEmpty()) return "_No matches for_ **" + rawTerm + "**.";
        StringBuilder sb = new StringBuilder("Matches for **").append(rawTerm).append("** (")
            .append(hits.size()).append("):");
        for (String h : hits.subList(0, Math.min(hits.size(), 15))) sb.append('\n').append(h);
        if (hits.size() > 15) sb.append("\n_…and ").append(hits.size() - 15).append(" more._");
        return sb.toString();
    }

    private static boolean matches(String m, String... needles) {
        for (String n : needles) {
            if (!m.contains(n)) {
                return false;
            }
        }
        return true;
    }

    private String categoryBreakdown(UUID orgId) {
        List<Asset> assets = assetRepository.findAllByOrganisationIdAndIsDeletedFalse(orgId, Pageable.unpaged()).getContent();
        Map<String, Long> byCat = assets.stream()
            .collect(Collectors.groupingBy(
                a -> a.getAssetCategory() != null ? a.getAssetCategory().getName() : "Uncategorised",
                Collectors.counting()));
        return formatMap("Asset distribution by **category**", byCat);
    }

    private String expiringCertificatesSummary(UUID orgId) {
        LocalDate horizon = LocalDate.now().plusDays(90);
        List<Certificate> certs = certificateRepository
            .findByAsset_Organisation_IdAndExpiryDateBeforeAndIsDeletedFalse(orgId, horizon);
        certs.sort(Comparator.comparing(Certificate::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())));
        int cap = 12;
        StringBuilder sb = new StringBuilder();
        sb.append("Certificates with expiry on or before **").append(horizon).append("**: **")
            .append(certs.size()).append("**.\n");
        int shown = 0;
        for (Certificate c : certs) {
            if (shown >= cap) {
                sb.append("\n_…and ").append(certs.size() - cap).append(" more._");
                break;
            }
            sb.append("\n• **").append(nullSafe(c.getCommonName())).append("** (")
                .append(c.getExpiryDate()).append(", ").append(c.getStatus()).append(")");
            shown++;
        }
        if (certs.isEmpty()) {
            sb.append("\n_No matching certificates._");
        }
        return sb.toString();
    }

    private String expiringLicencesSummary(UUID orgId) {
        LocalDate horizon = LocalDate.now().plusDays(90);
        List<Licence> list = licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId).stream()
            .filter(l -> l.getExpiryDate() != null && !l.getExpiryDate().isAfter(horizon))
            .sorted(Comparator.comparing(Licence::getExpiryDate))
            .toList();
        StringBuilder sb = new StringBuilder();
        sb.append("Licences expiring on or before **").append(horizon).append("**: **")
            .append(list.size()).append("**.\n");
        int cap = 12;
        int shown = 0;
        for (Licence l : list) {
            if (shown >= cap) {
                sb.append("\n_…and ").append(list.size() - cap).append(" more._");
                break;
            }
            String asset = l.getAsset() != null ? l.getAsset().getName() : "?";
            sb.append("\n• **").append(l.getLicenceType()).append("** on _").append(asset)
                .append("_ — expires **").append(l.getExpiryDate()).append("** (").append(l.getStatus()).append(")");
            shown++;
        }
        if (list.isEmpty()) {
            sb.append("\n_No licences in that window._");
        }
        return sb.toString();
    }

    private static String executiveNarrative(DashboardResponseDto d) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Executive snapshot**\n");
        sb.append("• Total assets: **").append(d.getTotalAssets()).append("**\n");
        sb.append("• Certificates expiring ≤30 days: **").append(d.getCertificatesExpiring30Days()).append("**\n");
        sb.append("• Licence seat-threshold alerts: **").append(d.getLicencesAboveSeatThreshold()).append("**\n");
        sb.append("• Overdue change-request reverts: **").append(d.getOverdueRevertsCount()).append("**\n");
        sb.append("• Access grants expiring soon: **").append(d.getAccessGrantsExpiringSoon()).append("**");
        return sb.toString();
    }

    private static String defaultHelp(DashboardResponseDto d) {
        return "I answer from **your live E-AMS data** (no external AI). Try asking:\n"
            + "• _How many assets are registered?_\n"
            + "• _List my assets_ / _Show recent assets_\n"
            + "• _Asset distribution by category_\n"
            + "• _Top critical assets_\n"
            + "• _Total asset cost / value_\n"
            + "• _List all certificates_ / _Certificates expiring soon_\n"
            + "• _List all licences_ / _Licences expiring soon_\n"
            + "• _Seat utilisation alerts_\n"
            + "• _Find <asset or certificate name>_ / _Who owns <asset>?_\n"
            + "• _How many users are in the system?_\n"
            + "• _Executive summary / dashboard overview_\n\n"
            + "**Quick facts:** " + d.getTotalAssets() + " assets; "
            + d.getCertificatesExpiring30Days() + " certs expiring in 30 days.";
    }

    private static String formatMap(String title, Map<String, Long> map) {
        if (map == null || map.isEmpty()) {
            return title + ": _no data._";
        }
        String body = map.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(e -> "• **" + e.getKey() + "**: " + e.getValue())
            .collect(Collectors.joining("\n"));
        return title + ":\n" + body;
    }

    private static String nullSafe(String s) {
        return s == null ? "(unnamed)" : s;
    }
}
