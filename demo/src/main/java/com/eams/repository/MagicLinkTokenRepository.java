package com.eams.repository;
import com.eams.model.MagicLinkToken;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkToken, UUID> {
    Optional<MagicLinkToken> findByTokenHashAndIsUsedFalse(String tokenHash);
    @Modifying
    @Query("UPDATE MagicLinkToken m SET m.isUsed = true WHERE m.user.id = :userId AND m.isUsed = false")
    void invalidateAllForUser(UUID userId);
}