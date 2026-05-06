package com.eams.repository;

import com.eams.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    List<Role> findByOrganisationIdAndIsDeletedFalse(UUID organisationId);
    Optional<Role> findByCodeAndOrganisationIdAndIsDeletedFalse(String code, UUID organisationId);
}
