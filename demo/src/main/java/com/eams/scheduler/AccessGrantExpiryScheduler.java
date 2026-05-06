package com.eams.scheduler;

import com.eams.service.AccessGrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessGrantExpiryScheduler {
    private final AccessGrantService accessGrantService;

    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void checkExpiredGrants() {
        log.info("Checking for expired access grants...");
        accessGrantService.escalateOverdueGrants();
    }
}
