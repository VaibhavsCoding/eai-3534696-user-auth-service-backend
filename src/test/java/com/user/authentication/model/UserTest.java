package com.user.authentication.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testGettersAndSetters() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        user.setId(1L);
        user.setEmail("test@example.com");
        user.setMobile("1234567890");
        user.setName("John Doe");
        user.setPassword("secret");
        user.setVerified(true);
        user.setIpAddress("127.0.0.1");
        user.setUserAgent("Mozilla/5.0");
        user.setDeviceName("Laptop");
        user.setDeviceOs("Windows");
        user.setDeviceFingerprint("fp123");
        user.setGeoCity("Berlin");
        user.setGeoCountry("Germany");
        user.setLastLoginIp("127.0.0.1");
        user.setLastLoginAt(now);
        user.setSessionId("sess123");
        user.setLastFailureReason("Wrong password");
        user.setFailedAttempts(2);
        user.setLockUntil(now.plusMinutes(5));
        user.setOtpSentCount(1);
        user.setOtpFailedCount(1);
        user.setLastOtpSentAt(now);
        user.setLastOtpVerifiedAt(now);
        user.setLastPasswordChangedAt(now);
        user.setLastPasswordResetAt(now);
        user.setPasswordChangeTokenId("token123");
        user.setTwoFactorEnabled(true);
        user.setRegistrationSource("Web");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertEquals(1L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("1234567890", user.getMobile());
        assertEquals("John Doe", user.getName());
        assertEquals("secret", user.getPassword());
        assertTrue(user.isVerified());
        assertEquals("127.0.0.1", user.getIpAddress());
        assertEquals("Mozilla/5.0", user.getUserAgent());
        assertEquals("Laptop", user.getDeviceName());
        assertEquals("Windows", user.getDeviceOs());
        assertEquals("fp123", user.getDeviceFingerprint());
        assertEquals("Berlin", user.getGeoCity());
        assertEquals("Germany", user.getGeoCountry());
        assertEquals("127.0.0.1", user.getLastLoginIp());
        assertEquals(now, user.getLastLoginAt());
        assertEquals("sess123", user.getSessionId());
        assertEquals("Wrong password", user.getLastFailureReason());
        assertEquals(2, user.getFailedAttempts());
        assertEquals(now.plusMinutes(5), user.getLockUntil());
        assertEquals(1, user.getOtpSentCount());
        assertEquals(1, user.getOtpFailedCount());
        assertEquals(now, user.getLastOtpSentAt());
        assertEquals(now, user.getLastOtpVerifiedAt());
        assertEquals(now, user.getLastPasswordChangedAt());
        assertEquals(now, user.getLastPasswordResetAt());
        assertEquals("token123", user.getPasswordChangeTokenId());
        assertTrue(user.isTwoFactorEnabled());
        assertEquals("Web", user.getRegistrationSource());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void testAccountLocking() {
        User user = new User();

        // initially not locked
        assertFalse(user.isAccountLocked());

        user.lockAccountForMinutes(5, "Failed login");
        assertTrue(user.isAccountLocked());
        assertEquals("Failed login", user.getLastFailureReason());

        user.unlockAccount();
        assertFalse(user.isAccountLocked());
        assertEquals(0, user.getFailedAttempts());
        assertNull(user.getLastFailureReason());
    }

    @Test
    void testFailedAttempts() {
        User user = new User();

        user.incrementFailedAttempts("Wrong password");
        assertEquals(1, user.getFailedAttempts());
        assertEquals("Wrong password", user.getLastFailureReason());

        user.resetFailedAttempts();
        assertEquals(0, user.getFailedAttempts());
        assertNull(user.getLastFailureReason());
    }

    @Test
    void testOtpMethods() throws InterruptedException {
        User user = new User();

        // mark OTP sent
        user.markOtpSent();
        assertEquals(1, user.getOtpSentCount());
        assertNotNull(user.getLastOtpSentAt());

        // mark OTP verified
        user.markOtpVerified();
        assertEquals(0, user.getOtpFailedCount());
        assertNotNull(user.getLastOtpVerifiedAt());

        // mark OTP failed
        user.markOtpFailed();
        assertEquals(1, user.getOtpFailedCount());
    }

    @Test
    void testMarkPasswordChanged() {
        User user = new User();
        user.markPasswordChanged();
        assertNotNull(user.getLastPasswordChangedAt());
    }
}
