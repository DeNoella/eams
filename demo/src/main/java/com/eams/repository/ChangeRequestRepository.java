package com.eams.repository;

import com.eams.enums.ChangeStatus;
import com.eams.model.ChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, UUID> {
    List<ChangeRequest> findByStatusAndScheduledRevertDateBefore(ChangeStatus status, LocalDate date);
    List<ChangeRequest> findByOrganisationIdAndStatusAndScheduledRevertDateBeforeAndIsDeletedFalse(UUID organisationId, ChangeStatus status, LocalDate date);
    Page<ChangeRequest> findByOrganisationIdAndIsDeletedFalse(UUID organisationId, Pageable pageable);
    Optional<ChangeRequest> findTopByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);
    Optional<ChangeRequest> findByIdAndOrganisationIdAndIsDeletedFalse(UUID id, UUID organisationId);
}
