package com.eams.service;

import com.eams.dto.request.LicenceCreateDto;
import com.eams.dto.response.LicenceResponseDto;
import com.eams.enums.LicenceStatus;
import com.eams.exception.ResourceNotFoundException;
import com.eams.model.Asset;
import com.eams.model.Licence;
import com.eams.repository.AssetRepository;
import com.eams.repository.LicenceRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LicenceService {
    private final LicenceRepository licenceRepository;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<LicenceResponseDto> getAllLicences(Pageable pageable) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        return licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public LicenceResponseDto getLicenceById(UUID id) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        Licence licence = licenceRepository.findByIdAndAsset_Organisation_IdAndIsDeletedFalse(id, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Licence not found"));
        return toDto(licence);
    }

    public LicenceResponseDto createLicence(LicenceCreateDto dto) {
        Licence licence = new Licence();
        apply(licence, dto);
        Licence saved = licenceRepository.save(licence);
        auditLogService.log(AuditLogService.AuditLogEntry.create(
            TenantContextHolder.getCurrentUserId(), "System", "licence", saved.getId(), saved.getLicenceType().name()));
        return toDto(saved);
    }

    public LicenceResponseDto updateLicence(UUID id, LicenceCreateDto dto) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        Licence licence = licenceRepository.findByIdAndAsset_Organisation_IdAndIsDeletedFalse(id, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Licence not found"));
        apply(licence, dto);
        return toDto(licenceRepository.save(licence));
    }

    public void deleteLicence(UUID id) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        Licence licence = licenceRepository.findByIdAndAsset_Organisation_IdAndIsDeletedFalse(id, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Licence not found"));
        licence.setIsDeleted(true);
        licenceRepository.save(licence);
        auditLogService.log(AuditLogService.AuditLogEntry.delete(
            TenantContextHolder.getCurrentUserId(), "System", "licence", id, licence.getLicenceType().name()));
    }

    public void incrementUsedSeats(UUID licenceId) {
        Licence l = licenceRepository.findById(licenceId).orElseThrow(() -> new ResourceNotFoundException("Licence not found"));
        l.setUsedSeats((l.getUsedSeats() == null ? 0 : l.getUsedSeats()) + 1);
    }

    public void decrementUsedSeats(UUID licenceId) {
        Licence l = licenceRepository.findById(licenceId).orElseThrow(() -> new ResourceNotFoundException("Licence not found"));
        int used = l.getUsedSeats() == null ? 0 : l.getUsedSeats();
        l.setUsedSeats(Math.max(0, used - 1));
    }

    @Transactional(readOnly = true)
    public Page<LicenceResponseDto> getExpiringWithin(int days, Pageable pageable) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        LocalDate horizon = LocalDate.now().plusDays(Math.max(0, days));
        List<Licence> list = licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId).stream()
            .filter(l -> l.getExpiryDate() != null && !l.getExpiryDate().isAfter(horizon))
            .sorted(Comparator.comparing(Licence::getExpiryDate))
            .toList();
        int start = (int) Math.min(pageable.getOffset(), list.size());
        int end = (int) Math.min(start + pageable.getPageSize(), list.size());
        List<LicenceResponseDto> slice = list.subList(start, end).stream().map(this::toDto).toList();
        return new org.springframework.data.domain.PageImpl<>(slice, pageable, list.size());
    }

    @Transactional(readOnly = true)
    public ExportFile exportLicences(String format, Integer expiringWithinDays) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        List<Licence> all = licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(orgId);
        if (expiringWithinDays != null) {
            LocalDate horizon = LocalDate.now().plusDays(Math.max(0, expiringWithinDays));
            all = all.stream()
                .filter(l -> l.getExpiryDate() != null && !l.getExpiryDate().isAfter(horizon))
                .sorted(Comparator.comparing(Licence::getExpiryDate))
                .toList();
        }
        String normalized = format == null ? "xlsx" : format.trim().toLowerCase();
        String dateSuffix = LocalDate.now().toString();
        return switch (normalized) {
            case "csv" -> new ExportFile(
                "goshen-licences-" + dateSuffix + ".csv",
                "text/csv",
                buildCsv(all));
            case "xlsx", "excel" -> new ExportFile(
                "goshen-licences-" + dateSuffix + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildExcel(all));
            default -> throw new IllegalArgumentException("Unsupported format: " + format + ". Use csv or xlsx.");
        };
    }

    private byte[] buildCsv(List<Licence> list) {
        StringBuilder b = new StringBuilder();
        b.append("Asset,Licence Type,Total Seats,Used Seats,Available Seats,Utilisation %,Expiry Date,Days Remaining,Status,Key/Reference\n");
        for (Licence l : list) {
            b.append(csv(l.getAsset() != null ? l.getAsset().getName() : "")).append(',')
                .append(csv(l.getLicenceType() != null ? l.getLicenceType().name() : "")).append(',')
                .append(l.getTotalSeats() == null ? "" : l.getTotalSeats()).append(',')
                .append(l.getUsedSeats() == null ? "" : l.getUsedSeats()).append(',')
                .append(l.getAvailableSeats() == null ? "" : l.getAvailableSeats()).append(',')
                .append(l.getUtilisationPct() == null ? "" : String.format("%.1f", l.getUtilisationPct())).append(',')
                .append(csv(l.getExpiryDate() != null ? l.getExpiryDate().toString() : "")).append(',')
                .append(l.getDaysRemaining()).append(',')
                .append(csv(l.getStatus() != null ? l.getStatus().name() : "")).append(',')
                .append(csv(l.getKeyOrReference()))
                .append('\n');
        }
        return b.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildExcel(List<Licence> list) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = wb.createSheet("Licences");
            String[] headers = {
                "Asset", "Licence Type", "Total Seats", "Used Seats", "Available",
                "Utilisation %", "Expiry Date", "Days Remaining", "Status", "Key/Reference"
            };
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            int rowIx = 1;
            for (Licence l : list) {
                Row row = sheet.createRow(rowIx++);
                row.createCell(0).setCellValue(l.getAsset() != null ? nvl(l.getAsset().getName()) : "");
                row.createCell(1).setCellValue(l.getLicenceType() != null ? l.getLicenceType().name() : "");
                row.createCell(2).setCellValue(l.getTotalSeats() == null ? 0 : l.getTotalSeats());
                row.createCell(3).setCellValue(l.getUsedSeats() == null ? 0 : l.getUsedSeats());
                row.createCell(4).setCellValue(l.getAvailableSeats() == null ? 0 : l.getAvailableSeats());
                row.createCell(5).setCellValue(l.getUtilisationPct() == null ? 0.0 : l.getUtilisationPct());
                row.createCell(6).setCellValue(l.getExpiryDate() != null ? l.getExpiryDate().toString() : "");
                row.createCell(7).setCellValue(l.getDaysRemaining());
                row.createCell(8).setCellValue(l.getStatus() != null ? l.getStatus().name() : "");
                row.createCell(9).setCellValue(nvl(l.getKeyOrReference()));
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate licence Excel export", e);
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

    public void updateLicenceStatuses() {
        licenceRepository.findAll().forEach(l -> {
            long days = l.getDaysRemaining();
            if (days <= 0) l.setStatus(LicenceStatus.EXPIRED);
            else if (days <= 30) l.setStatus(LicenceStatus.CRITICAL);
            else if (days <= 90) l.setStatus(LicenceStatus.WARNING);
            else if (days <= 180) l.setStatus(LicenceStatus.REVIEW_SOON);
            else l.setStatus(LicenceStatus.VALID);
        });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getComplianceReport(UUID organisationId) {
        List<Licence> all = licenceRepository.findByAsset_Organisation_IdAndIsDeletedFalse(organisationId);
        long overloaded = all.stream().filter(l -> l.getTotalSeats() != null && l.getUsedSeats() != null && l.getUsedSeats() > l.getTotalSeats()).count();
        return Map.of("totalLicences", all.size(), "overAllocated", overloaded);
    }

    private void apply(Licence l, LicenceCreateDto dto) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        Asset asset = assetRepository.findByIdAndOrganisationIdAndIsDeletedFalse(dto.getAssetId(), orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        l.setAsset(asset);
        l.setLicenceType(dto.getLicenceType());
        l.setTotalSeats(dto.getTotalSeats());
        l.setUsedSeats(dto.getUsedSeats() == null ? 0 : dto.getUsedSeats());
        l.setExpiryDate(dto.getExpiryDate());
        l.setStatus(dto.getStatus() == null ? LicenceStatus.VALID : dto.getStatus());
        l.setRenewalLeadDays(dto.getRenewalLeadDays());
        l.setSoftwareAssuranceExpiry(dto.getSoftwareAssuranceExpiry());
        l.setKeyOrReference(dto.getKeyOrReference());
        l.setSeatAlertThresholdPct(dto.getSeatAlertThresholdPct() == null ? 85 : dto.getSeatAlertThresholdPct());
    }

    private LicenceResponseDto toDto(Licence l) {
        return LicenceResponseDto.builder()
            .id(l.getId())
            .assetId(l.getAsset() != null ? l.getAsset().getId() : null)
            .assetName(l.getAsset() != null ? l.getAsset().getName() : null)
            .licenceType(l.getLicenceType())
            .totalSeats(l.getTotalSeats())
            .usedSeats(l.getUsedSeats())
            .availableSeats(l.getAvailableSeats())
            .utilisationPct(l.getUtilisationPct())
            .expiryDate(l.getExpiryDate())
            .softwareAssuranceExpiry(l.getSoftwareAssuranceExpiry())
            .status(l.getStatus())
            .renewalLeadDays(l.getRenewalLeadDays())
            .seatAlertThresholdPct(l.getSeatAlertThresholdPct())
            .keyOrReference(l.getKeyOrReference())
            .daysRemaining(l.getDaysRemaining())
            .build();
    }
}
