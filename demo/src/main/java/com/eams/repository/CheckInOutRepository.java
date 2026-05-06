package com.eams.repository;

import com.eams.enums.TransactionStatus;
import com.eams.model.CheckInOutTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CheckInOutRepository extends JpaRepository<CheckInOutTransaction, UUID> {
    List<CheckInOutTransaction> findByAssetIdAndIsDeletedFalse(UUID assetId);
    List<CheckInOutTransaction> findByStatusAndExpectedReturnDateBefore(TransactionStatus status, LocalDate date);
}
