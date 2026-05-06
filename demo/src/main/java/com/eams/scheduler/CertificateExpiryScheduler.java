package com.eams.scheduler;

import com.eams.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateExpiryScheduler {
    private final CertificateService certificateService;

    @Scheduled(cron = "0 0 2 * * *")
    public void updateCertificateStatuses() {
        log.info("Running certificate expiry check...");
        certificateService.updateCertificateStatuses();
    }
}
