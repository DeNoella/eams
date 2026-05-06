package com.eams.repository;
import com.eams.model.User;

import org.springframework.data.jpa.repository.*;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    Optional<User> findByEmailIgnoreCaseAndIsDeletedFalse(String email);
    boolean existsByEmailIgnoreCaseAndIsDeletedFalse(String email);
    Optional<User> findByIdAndOrganisationIdAndIsDeletedFalse(UUID id, UUID organisationId);
    Page<User> findAllByOrganisationIdAndIsDeletedFalse(UUID organisationId, Pageable pageable);

    long countByOrganisation_Id(UUID organisationId);
}