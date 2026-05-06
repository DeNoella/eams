package com.eams.repository;

import com.eams.model.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, UUID> {
    List<AssetCategory> findByOrganisationIdAndIsActiveTrueAndIsDeletedFalse(UUID organisationId);
    Optional<AssetCategory> findByIdAndOrganisationIdAndIsDeletedFalse(UUID id, UUID organisationId);
}
