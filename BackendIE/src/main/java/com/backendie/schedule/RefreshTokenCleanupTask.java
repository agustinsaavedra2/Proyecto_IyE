package com.backendie.schedule;

import com.backendie.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupTask {

    private final RefreshTokenService refreshTokenService;
    private final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupTask.class);

    // Run once a day at 02:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        try {
            int deleted = refreshTokenService.deleteExpired();
            log.info("RefreshTokenCleanupTask removed {} expired refresh tokens", deleted);
        } catch (Exception e) {
            log.error("Error while cleaning expired refresh tokens", e);
        }
    }
}

