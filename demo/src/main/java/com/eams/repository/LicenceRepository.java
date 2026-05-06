package com.eams.repository;
import com.eams.model.Licence;
import com.eams.enums.LicenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.*;
public interface LicenceRepository extends JpaRepository<Licence, UUID> {
    List<Licence> findByAsset_Organisation_IdAndIsDeletedFalse(UUID organisationId);
    Page<Licence> findByAsset_Organisation_IdAndIsDeletedFalse(UUID organisationId, Pageable pageable);
    Optional<Licence> findByIdAndAsset_Organisation_IdAndIsDeletedFalse(UUID id, UUID organisationId);
    List<Licence> findByAsset_Organisation_IdAndStatusAndIsDeletedFalse(UUID organisationId, LicenceStatus status);
}
