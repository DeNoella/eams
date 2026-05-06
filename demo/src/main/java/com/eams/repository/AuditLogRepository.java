package com.eams.repository;
import com.eams.model.AuditLog;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {
    Page<AuditLog> findAllByOrganisationId(UUID organisationId, Pageable pageable);

    long countByOrganisation_IdAndEventTimestampBetween(UUID organisationId, OffsetDateTime start, OffsetDateTime end);
}