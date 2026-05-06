package com.eams.repository;

import com.eams.enums.AccessGrantStatus;
import com.eams.model.AccessGrant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessGrantRepository extends JpaRepository<AccessGrant, UUID> {
    List<AccessGrant> findByStatusAndExpiresAtBefore(AccessGrantStatus status, OffsetDateTime dateTime);
    List<AccessGrant> findByOrganisationIdAndStatusAndExpiresAtBeforeAndIsDeletedFalse(UUID organisationId, AccessGrantStatus status, OffsetDateTime dateTime);
    Page<AccessGrant> findByOrganisationIdAndIsDeletedFalse(UUID organisationId, Pageable pageable);
    Optional<AccessGrant> findByIdAndOrganisationIdAndIsDeletedFalse(UUID id, UUID organisationId);
}
