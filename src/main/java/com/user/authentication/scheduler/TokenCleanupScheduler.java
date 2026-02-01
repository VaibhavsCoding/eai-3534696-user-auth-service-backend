package com.user.authentication.scheduler;

import com.user.authentication.repository.OtpTokenRepository;
import com.user.authentication.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final OtpTokenRepository otpRepo;
    private final PasswordResetTokenRepository resetRepo;

    /**
     * Runs every 5 minutes to remove expired OTP and reset tokens.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();

            int otpDeleted = otpRepo.deleteByExpiresAtBefore(now);
            int resetDeleted = resetRepo.deleteByExpiresAtBefore(now);

            if (otpDeleted > 0 || resetDeleted > 0) {
                log.info("🧹 Cleaned {} expired OTPs and {} reset tokens at {}", otpDeleted, resetDeleted, now);
            } else {
                log.debug("No expired tokens to clean at {}", now);
            }

        } catch (Exception e) {
            log.error("Error occurred during token cleanup: {}", e.getMessage(), e);
        }
    }
}
