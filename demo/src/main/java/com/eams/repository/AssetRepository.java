package com.eams.repository;
import com.eams.model.Asset;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import java.util.*;
public interface AssetRepository extends JpaRepository<Asset, UUID>, JpaSpecificationExecutor<Asset> {
    Optional<Asset> findByIdAndOrganisationIdAndIsDeletedFalse(UUID id, UUID organisationId);
    Page<Asset> findAllByOrganisationIdAndIsDeletedFalse(UUID organisationId, Pageable pageable);
    boolean existsByOrganisationIdAndAssetIdDisplay(UUID organisationId, String assetIdDisplay);
    long countByOrganisationIdAndAssetIdDisplayStartingWith(UUID organisationId, String prefix);
    List<Asset> findByAssignedUserIdAndOrganisationIdAndIsDeletedFalse(UUID userId, UUID organisationId);
}