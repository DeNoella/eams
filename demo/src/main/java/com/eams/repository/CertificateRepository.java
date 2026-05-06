package com.eams.repository;
import com.eams.model.Certificate;
import com.eams.enums.CertificateStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.*;
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    List<Certificate> findByAsset_Organisation_IdAndExpiryDateBeforeAndIsDeletedFalse(
        UUID organisationId, LocalDate date);
    List<Certificate> findByAsset_Organisation_IdAndStatusAndIsDeletedFalse(
        UUID organisationId, CertificateStatus status);
    Optional<Certificate> findByIdAndAsset_Organisation_IdAndIsDeletedFalse(UUID id, UUID organisationId);
    Page<Certificate> findByAsset_Organisation_IdAndIsDeletedFalse(UUID organisationId, Pageable pageable);
    Page<Certificate> findByAsset_Organisation_IdAndStatusAndIsDeletedFalse(
        UUID organisationId, CertificateStatus status, Pageable pageable);
    Page<Certificate> findByAsset_Organisation_IdAndExpiryDateLessThanEqualAndIsDeletedFalse(
        UUID organisationId, LocalDate date, Pageable pageable);
}