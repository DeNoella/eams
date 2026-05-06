package com.eams.repository;
import com.eams.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {
    Optional<Organisation> findBySlugAndIsDeletedFalse(String slug);
}