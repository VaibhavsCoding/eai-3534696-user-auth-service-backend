package com.user.authentication.service;

import com.user.authentication.model.LoginHistory;
import com.user.authentication.model.User;
import com.user.authentication.repository.LoginHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(LoginHistoryService.class);
    private final LoginHistoryRepository loginHistoryRepository;

    @Value("${security.login-history.limit:5}")
    int maxLoginHistory;

    /**
     * Count successful login attempts for this user
     */
    public long countSuccessfulLogins(User user) {
        try {
            Optional<LoginHistory> lastSuccessOpt = loginHistoryRepository.findTopByUserAndSuccessTrueOrderByLoginAtDesc(user);
            return lastSuccessOpt.map(LoginHistory::getTotalSuccessfulLogins).orElse(0);
        } catch (Exception e) {
            logger.error("❌ Failed to count successful logins for {}: {}", user.getEmail(), e.getMessage());
            return 0;
        }
    }

    /**
     * Record login attempt (success or failure).
     * This method is transactional so the saved entry is visible within the tx.
     */
    @Transactional
    public void recordLogin(User user, boolean success, String reason) {
        try {
            long prevSuccess = countSuccessfulLogins(user);
            int newTotal = success ? (int) (prevSuccess + 1) : (int) prevSuccess;

            LoginHistory entry;
            if (success) {
                entry = LoginHistory.successEntry(
                        user,
                        newTotal,
                        user.getLastLoginIp(),
                        user.getUserAgent(),
                        user.getDeviceOs(),
                        user.getDeviceName(),
                        user.getDeviceFingerprint(),
                        user.getGeoCity(),
                        user.getGeoCountry()
                );
                logger.info("✅ Recording successful login for {} (total_successful_logins will be {})", user.getEmail(), newTotal);
            } else {
                entry = LoginHistory.failureEntry(
                        user,
                        user.getLastLoginIp(),
                        user.getUserAgent(),
                        reason,
                        user.getDeviceOs(),
                        user.getDeviceName(),
                        user.getDeviceFingerprint(),
                        user.getGeoCity(),
                        user.getGeoCountry(),
                        newTotal
                );
                logger.info("❌ Recording failed login for {} (reason={})", user.getEmail(), reason);
            }

            // Save the new entry
            loginHistoryRepository.save(entry);

            // Trim old records after save so we keep only the latest N (atomic within tx)
            trimOldEntries(user);

        } catch (Exception e) {
            logger.error("💥 Error recording login for {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Record logout. It updates the most recent successful login record's logoutAt.
     */
    @Transactional
    public void recordLogout(User user, String reason) {
        try {
            Optional<LoginHistory> lastLoginOpt = loginHistoryRepository.findTopByUserAndSuccessTrueOrderByLoginAtDesc(user);

            if (lastLoginOpt.isPresent()) {
                LoginHistory lastLogin = lastLoginOpt.get();
                lastLogin.setLogoutAt(LocalDateTime.now());
                lastLogin.setFailureReason(reason != null ? reason : "User logged out");
                loginHistoryRepository.save(lastLogin);
                logger.info("📗 Logout recorded for {}", user.getEmail());
            } else {
                // Fallback: create a logout entry if no successful login exists
                LoginHistory logoutEntry = LoginHistory.logoutEntry(
                        user,
                        user.getLastLoginIp(),
                        user.getUserAgent(),
                        reason != null ? reason : "Logout without prior login",
                        user.getDeviceOs(),
                        user.getDeviceName(),
                        user.getDeviceFingerprint(),
                        user.getGeoCity(),
                        user.getGeoCountry()
                );
                loginHistoryRepository.save(logoutEntry);
                logger.warn("⚠️ Created fallback logout entry for {}", user.getEmail());
            }
        } catch (Exception e) {
            logger.error("💥 Error while recording logout for {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Keep only latest N login entries per user (N = maxLoginHistory).
     * Runs in the same transaction as the save above to avoid race conditions.
     */
    @Transactional
    protected void trimOldEntries(User user) {
        List<LoginHistory> allEntries = loginHistoryRepository.findByUserOrderByLoginAtDesc(user);
        if (allEntries.size() > maxLoginHistory) {
            List<LoginHistory> toDelete = allEntries.subList(maxLoginHistory, allEntries.size());
            loginHistoryRepository.deleteAll(toDelete);
            logger.debug("🧹 Trimmed old login history for {} (kept {} entries)", user.getEmail(), maxLoginHistory);
        }
    }
}