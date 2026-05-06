package com.eams.scheduler;

import com.eams.service.ChangeRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeRequestRevertScheduler {
    private final ChangeRequestService changeRequestService;

    @Scheduled(cron = "0 0 3 * * *")
    public void flagOverdueReverts() {
        log.info("Checking for overdue change reverts...");
        changeRequestService.flagOverdueReverts();
    }
}
