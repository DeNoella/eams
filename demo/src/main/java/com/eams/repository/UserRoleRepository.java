package com.eams.repository;
import com.eams.model.UserRole;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    @Query("SELECT r.code FROM UserRole ur JOIN ur.role r WHERE ur.user.id = :userId AND ur.isDeleted = false")
    List<String> findRoleCodesByUserId(UUID userId);
}