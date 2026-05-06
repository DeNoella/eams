package com.eams.service;

import com.eams.dto.request.AssetCreateDto;
import com.eams.dto.response.AssetResponseDto;
import com.eams.exception.ResourceNotFoundException;
import com.eams.model.*;
import com.eams.repository.*;
import com.eams.security.TenantContextHolder;
import com.eams.util.AssetIdGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final LocationRepository locationRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final AuditLogService auditLogService;
    private final AssetIdGenerator assetIdGenerator;

    @Transactional(readOnly = true)
    public Page<AssetResponseDto> getAllAssets(Pageable pageable, String search) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        return findAssets(orgId, pageable, search)
                .map(this::toResponseDto);
    }

    @Transactional(readOnly = true)
    public AssetResponseDto getAssetById(UUID assetId) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        Asset asset = assetRepository
            .findByIdAndOrganisationIdAndIsDeletedFalse(assetId, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));
        return toResponseDto(asset);
    }

    public AssetResponseDto createAsset(AssetCreateDto dto) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        UUID actorId = TenantContextHolder.getCurrentUserId();

        Organisation organisation = organisationRepository.findById(orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));

        AssetCategory category = assetCategoryRepository
            .findByIdAndOrganisationIdAndIsDeletedFalse(
                dto.getAssetCategoryId(), orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset category not found"));

        // Generate asset ID
        String locationCode = "GEN";
        if (dto.getLocationId() != null) {
            locationCode = locationRepository.findById(dto.getLocationId())
                .map(Location::getCode).orElse("GEN");
        }

        String assetIdDisplay = assetIdGenerator.generate(
            category.getIdFormat(), category.getCode(), locationCode, orgId);

        Asset asset = Asset.builder()
            .organisation(organisation)
            .assetIdDisplay(assetIdDisplay)
            .assetCategory(category)
            .name(dto.getName())
            .description(dto.getDescription())
            .criticality(dto.getCriticality())
            .status(dto.getStatus())
            .lifecycleStage(dto.getLifecycleStage())
            .yearOfInstallation(dto.getYearOfInstallation())
            .acquisitionCost(dto.getAcquisitionCost())
            .currencyCode(dto.getCurrencyCode() != null ? dto.getCurrencyCode() : "USD")
            .annualCost(dto.getAnnualCost())
            .purchaseOrderRef(dto.getPurchaseOrderRef())
            .vendorName(dto.getVendorName())
            .supportContractRef(dto.getSupportContractRef())
            .supportExpiryDate(dto.getSupportExpiryDate())
            .warrantyExpiryDate(dto.getWarrantyExpiryDate())
            .notes(dto.getNotes())
            .barcode(dto.getBarcode())
            .versionNo(dto.getVersionNo())
            .versionDate(dto.getVersionDate())
            .numberOfLicences(dto.getNumberOfLicences())
            .hostingInstitution(dto.getHostingInstitution())
            .functionsOfSystem(dto.getFunctionsOfSystem())
            .staffInCharge(dto.getStaffInCharge())
            .serverType(dto.getServerType())
            .operatingSystemName(dto.getOperatingSystemName())
            .osVendorName(dto.getOsVendorName())
            .osVersionNo(dto.getOsVersionNo())
            .modelNumber(dto.getModelNumber())
            .equipmentLicenceType(dto.getEquipmentLicenceType())
            .build();

        // Set optional relationships
        if (dto.getLocationId() != null) {
            asset.setLocation(locationRepository.findById(dto.getLocationId()).orElse(null));
        }
        if (dto.getDepartmentId() != null) {
            asset.setDepartment(departmentRepository.findById(dto.getDepartmentId()).orElse(null));
        }
        if (dto.getTechnicalOwnerId() != null) {
            asset.setTechnicalOwner(userRepository.findById(dto.getTechnicalOwnerId()).orElse(null));
        }
        if (dto.getBusinessOwnerId() != null) {
            asset.setBusinessOwner(userRepository.findById(dto.getBusinessOwnerId()).orElse(null));
        }

        Asset saved = assetRepository.save(asset);

        // Audit log
        auditLogService.log(AuditLogService.AuditLogEntry.create(
            actorId, "System", "asset", saved.getId(), saved.getName()));

        return toResponseDto(saved);
    }

    public AssetResponseDto updateAsset(UUID assetId, AssetCreateDto dto) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        UUID actorId = TenantContextHolder.getCurrentUserId();

        Asset asset = assetRepository
            .findByIdAndOrganisationIdAndIsDeletedFalse(assetId, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        // Log each field change
        if (!asset.getName().equals(dto.getName())) {
            auditLogService.log(AuditLogService.AuditLogEntry.update(
                actorId, "System", "asset", assetId, asset.getName(),
                "name", asset.getName(), dto.getName()));
        }

        AssetCategory category = assetCategoryRepository
            .findByIdAndOrganisationIdAndIsDeletedFalse(dto.getAssetCategoryId(), orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset category not found"));
        asset.setAssetCategory(category);
        asset.setName(dto.getName());
        asset.setDescription(dto.getDescription());
        asset.setCriticality(dto.getCriticality());
        asset.setStatus(dto.getStatus());
        asset.setLifecycleStage(dto.getLifecycleStage());
        asset.setYearOfInstallation(dto.getYearOfInstallation());
        asset.setNotes(dto.getNotes());
        asset.setVendorName(dto.getVendorName());
        asset.setAcquisitionCost(dto.getAcquisitionCost());
        asset.setCurrencyCode(dto.getCurrencyCode() != null ? dto.getCurrencyCode() : asset.getCurrencyCode());
        asset.setAnnualCost(dto.getAnnualCost());
        asset.setPurchaseOrderRef(dto.getPurchaseOrderRef());
        asset.setSupportContractRef(dto.getSupportContractRef());
        asset.setSupportExpiryDate(dto.getSupportExpiryDate());
        asset.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());
        asset.setBarcode(dto.getBarcode());
        asset.setVersionNo(dto.getVersionNo());
        asset.setVersionDate(dto.getVersionDate());
        asset.setNumberOfLicences(dto.getNumberOfLicences());
        asset.setHostingInstitution(dto.getHostingInstitution());
        asset.setFunctionsOfSystem(dto.getFunctionsOfSystem());
        asset.setStaffInCharge(dto.getStaffInCharge());
        asset.setServerType(dto.getServerType());
        asset.setOperatingSystemName(dto.getOperatingSystemName());
        asset.setOsVendorName(dto.getOsVendorName());
        asset.setOsVersionNo(dto.getOsVersionNo());
        asset.setModelNumber(dto.getModelNumber());
        asset.setEquipmentLicenceType(dto.getEquipmentLicenceType());
        if (dto.getLocationId() != null) {
            asset.setLocation(locationRepository.findById(dto.getLocationId()).orElse(null));
        }
        if (dto.getDepartmentId() != null) {
            asset.setDepartment(departmentRepository.findById(dto.getDepartmentId()).orElse(null));
        }
        if (dto.getTechnicalOwnerId() != null) {
            asset.setTechnicalOwner(userRepository.findById(dto.getTechnicalOwnerId()).orElse(null));
        }
        if (dto.getBusinessOwnerId() != null) {
            asset.setBusinessOwner(userRepository.findById(dto.getBusinessOwnerId()).orElse(null));
        }

        Asset saved = assetRepository.save(asset);
        return toResponseDto(saved);
    }

    public void deleteAsset(UUID assetId) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        UUID actorId = TenantContextHolder.getCurrentUserId();

        Asset asset = assetRepository
            .findByIdAndOrganisationIdAndIsDeletedFalse(assetId, orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + assetId));

        asset.setIsDeleted(true);
        assetRepository.save(asset);

        auditLogService.log(AuditLogService.AuditLogEntry.delete(
            actorId, "System", "asset", assetId, asset.getName()));
    }

    @Transactional(readOnly = true)
    public ExportFile exportAssets(String format, String search) {
        UUID orgId = TenantContextHolder.getCurrentTenantId();
        List<Asset> assets = findAssets(orgId, Pageable.unpaged(), search).getContent();
        String normalizedFormat = format == null ? "xlsx" : format.trim().toLowerCase();
        String dateSuffix = LocalDate.now().toString();
        return switch (normalizedFormat) {
            case "csv" -> new ExportFile(
                "goshen-assets-" + dateSuffix + ".csv",
                "text/csv",
                buildCsv(assets));
            case "xlsx", "excel" -> new ExportFile(
                "goshen-assets-" + dateSuffix + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildExcel(assets));
            default -> throw new IllegalArgumentException("Unsupported format: " + format + ". Use csv or xlsx.");
        };
    }

    private Page<Asset> findAssets(UUID orgId, Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            return assetRepository.findAllByOrganisationIdAndIsDeletedFalse(orgId, pageable);
        }
        String query = "%" + search.trim().toLowerCase() + "%";
        Specification<Asset> specification = (root, cq, cb) -> cb.and(
            cb.equal(root.get("organisation").get("id"), orgId),
            cb.isFalse(root.get("isDeleted")),
            cb.or(
                cb.like(cb.lower(root.get("name")), query),
                cb.like(cb.lower(root.get("assetIdDisplay")), query),
                cb.like(cb.lower(cb.coalesce(root.get("vendorName"), "")), query),
                cb.like(cb.lower(cb.coalesce(root.get("barcode"), "")), query)
            )
        );
        return assetRepository.findAll(specification, pageable);
    }

    private byte[] buildCsv(List<Asset> assets) {
        StringBuilder builder = new StringBuilder();
        builder.append("Asset ID,Name,Category,Status,Criticality,Location,Department,Assigned User,Vendor,Warranty Expiry,Support Expiry\n");
        for (Asset asset : assets) {
            builder.append(csv(asset.getAssetIdDisplay())).append(',')
                .append(csv(asset.getName())).append(',')
                .append(csv(asset.getAssetCategory() != null ? asset.getAssetCategory().getName() : ""))
                .append(',')
                .append(csv(asset.getStatus() != null ? asset.getStatus().name() : ""))
                .append(',')
                .append(csv(asset.getCriticality() != null ? asset.getCriticality().name() : ""))
                .append(',')
                .append(csv(asset.getLocation() != null ? asset.getLocation().getName() : ""))
                .append(',')
                .append(csv(asset.getDepartment() != null ? asset.getDepartment().getName() : ""))
                .append(',')
                .append(csv(asset.getAssignedUser() != null ? asset.getAssignedUser().getFullName() : ""))
                .append(',')
                .append(csv(asset.getVendorName()))
                .append(',')
                .append(csv(asset.getWarrantyExpiryDate() != null ? asset.getWarrantyExpiryDate().toString() : ""))
                .append(',')
                .append(csv(asset.getSupportExpiryDate() != null ? asset.getSupportExpiryDate().toString() : ""))
                .append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] buildExcel(List<Asset> assets) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Assets");
            Row header = sheet.createRow(0);
            String[] headers = {
                "Asset ID", "Name", "Category", "Status", "Criticality", "Location",
                "Department", "Assigned User", "Vendor", "Warranty Expiry", "Support Expiry"
            };
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            int rowIndex = 1;
            for (Asset asset : assets) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(defaultString(asset.getAssetIdDisplay()));
                row.createCell(1).setCellValue(defaultString(asset.getName()));
                row.createCell(2).setCellValue(asset.getAssetCategory() != null ? defaultString(asset.getAssetCategory().getName()) : "");
                row.createCell(3).setCellValue(asset.getStatus() != null ? asset.getStatus().name() : "");
                row.createCell(4).setCellValue(asset.getCriticality() != null ? asset.getCriticality().name() : "");
                row.createCell(5).setCellValue(asset.getLocation() != null ? defaultString(asset.getLocation().getName()) : "");
                row.createCell(6).setCellValue(asset.getDepartment() != null ? defaultString(asset.getDepartment().getName()) : "");
                row.createCell(7).setCellValue(asset.getAssignedUser() != null ? defaultString(asset.getAssignedUser().getFullName()) : "");
                row.createCell(8).setCellValue(defaultString(asset.getVendorName()));
                row.createCell(9).setCellValue(asset.getWarrantyExpiryDate() != null ? asset.getWarrantyExpiryDate().toString() : "");
                row.createCell(10).setCellValue(asset.getSupportExpiryDate() != null ? asset.getSupportExpiryDate().toString() : "");
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    private String csv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    public record ExportFile(String filename, String contentType, byte[] content) {}

    private AssetResponseDto toResponseDto(Asset asset) {
        return AssetResponseDto.builder()
            .id(asset.getId())
            .assetCategoryId(asset.getAssetCategory() != null ? asset.getAssetCategory().getId() : null)
            .assetIdDisplay(asset.getAssetIdDisplay())
            .name(asset.getName())
            .description(asset.getDescription())
            .categoryName(asset.getAssetCategory() != null ? asset.getAssetCategory().getName() : null)
            .categoryCode(asset.getAssetCategory() != null ? asset.getAssetCategory().getCode() : null)
            .locationId(asset.getLocation() != null ? asset.getLocation().getId() : null)
            .locationName(asset.getLocation() != null ? asset.getLocation().getName() : null)
            .departmentId(asset.getDepartment() != null ? asset.getDepartment().getId() : null)
            .departmentName(asset.getDepartment() != null ? asset.getDepartment().getName() : null)
            .assignedUserId(asset.getAssignedUser() != null ? asset.getAssignedUser().getId() : null)
            .assignedUserName(asset.getAssignedUser() != null ? asset.getAssignedUser().getFullName() : null)
            .technicalOwnerId(asset.getTechnicalOwner() != null ? asset.getTechnicalOwner().getId() : null)
            .technicalOwnerName(asset.getTechnicalOwner() != null ? asset.getTechnicalOwner().getFullName() : null)
            .businessOwnerId(asset.getBusinessOwner() != null ? asset.getBusinessOwner().getId() : null)
            .businessOwnerName(asset.getBusinessOwner() != null ? asset.getBusinessOwner().getFullName() : null)
            .criticality(asset.getCriticality())
            .status(asset.getStatus())
            .lifecycleStage(asset.getLifecycleStage())
            .yearOfInstallation(asset.getYearOfInstallation())
            .acquisitionCost(asset.getAcquisitionCost())
            .currencyCode(asset.getCurrencyCode())
            .annualCost(asset.getAnnualCost())
            .vendorName(asset.getVendorName())
            .purchaseOrderRef(asset.getPurchaseOrderRef())
            .supportContractRef(asset.getSupportContractRef())
            .warrantyExpiryDate(asset.getWarrantyExpiryDate())
            .supportExpiryDate(asset.getSupportExpiryDate())
            .daysUntilWarrantyExpiry(AssetResponseDto.daysUntil(asset.getWarrantyExpiryDate()))
            .daysUntilSupportExpiry(AssetResponseDto.daysUntil(asset.getSupportExpiryDate()))
            .lastAuditDate(asset.getLastAuditDate())
            .barcode(asset.getBarcode())
            .notes(asset.getNotes())
            .versionNo(asset.getVersionNo())
            .versionDate(asset.getVersionDate())
            .numberOfLicences(asset.getNumberOfLicences())
            .hostingInstitution(asset.getHostingInstitution())
            .functionsOfSystem(asset.getFunctionsOfSystem())
            .staffInCharge(asset.getStaffInCharge())
            .serverType(asset.getServerType())
            .operatingSystemName(asset.getOperatingSystemName())
            .osVendorName(asset.getOsVendorName())
            .osVersionNo(asset.getOsVersionNo())
            .modelNumber(asset.getModelNumber())
            .equipmentLicenceType(asset.getEquipmentLicenceType())
            .createdAt(asset.getCreatedAt())
            .updatedAt(asset.getUpdatedAt())
            .build();
    }
}