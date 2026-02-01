package com.user.authentication.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LoginHistoryTest {

    @Test
    void testSuccessEntry() {
        User mockUser = new User();
        int totalLogins = 5;

        LoginHistory entry = LoginHistory.successEntry(
                mockUser,
                totalLogins,
                "127.0.0.1",
                "Mozilla/5.0",
                "Windows",
                "Laptop",
                "fingerprint123",
                "Berlin",
                "Germany"
        );

        assertNotNull(entry);
        assertEquals(mockUser, entry.getUser());
        assertEquals("127.0.0.1", entry.getIpAddress());
        assertEquals("Mozilla/5.0", entry.getUserAgent());
        assertEquals("Windows", entry.getDeviceOs());
        assertEquals("Laptop", entry.getDeviceName());
        assertEquals("fingerprint123", entry.getDeviceFingerprint());
        assertEquals("Berlin", entry.getGeoCity());
        assertEquals("Germany", entry.getGeoCountry());
        assertTrue(entry.isSuccess());
        assertNull(entry.getFailureReason());
        assertEquals(totalLogins, entry.getTotalSuccessfulLogins());
        assertNotNull(entry.getLoginAt());
        assertNull(entry.getLogoutAt());
    }

    @Test
    void testFailureEntry() {
        User mockUser = new User();
        int totalLogins = 3;
        String reason = "Invalid password";

        LoginHistory entry = LoginHistory.failureEntry(
                mockUser,
                "192.168.0.1",
                "Mozilla/5.0",
                reason,
                "Linux",
                "Desktop",
                "fingerprint456",
                "Paris",
                "France",
                totalLogins
        );

        assertNotNull(entry);
        assertEquals(mockUser, entry.getUser());
        assertEquals("192.168.0.1", entry.getIpAddress());
        assertEquals("Mozilla/5.0", entry.getUserAgent());
        assertEquals("Linux", entry.getDeviceOs());
        assertEquals("Desktop", entry.getDeviceName());
        assertEquals("fingerprint456", entry.getDeviceFingerprint());
        assertEquals("Paris", entry.getGeoCity());
        assertEquals("France", entry.getGeoCountry());
        assertFalse(entry.isSuccess());
        assertEquals(reason, entry.getFailureReason());
        assertEquals(totalLogins, entry.getTotalSuccessfulLogins());
        assertNotNull(entry.getLoginAt());
        assertNull(entry.getLogoutAt());
    }

    @Test
    void testLogoutEntry() {
        User mockUser = new User();
        String reason = "User clicked logout";

        LoginHistory entry = LoginHistory.logoutEntry(
                mockUser,
                "10.0.0.1",
                "Safari/14",
                reason,
                "macOS",
                "MacBook",
                "fingerprint789",
                "London",
                "UK"
        );

        assertNotNull(entry);
        assertEquals(mockUser, entry.getUser());
        assertEquals("10.0.0.1", entry.getIpAddress());
        assertEquals("Safari/14", entry.getUserAgent());
        assertEquals("macOS", entry.getDeviceOs());
        assertEquals("MacBook", entry.getDeviceName());
        assertEquals("fingerprint789", entry.getDeviceFingerprint());
        assertEquals("London", entry.getGeoCity());
        assertEquals("UK", entry.getGeoCountry());
        assertFalse(entry.isSuccess());
        assertEquals(reason, entry.getFailureReason());
        assertEquals(0, entry.getTotalSuccessfulLogins());
        assertNotNull(entry.getLoginAt());
        assertNotNull(entry.getLogoutAt());
    }

    @Test
    void testSettersAndGetters() {
        LoginHistory entry = new LoginHistory();
        User mockUser = new User();
        LocalDateTime now = LocalDateTime.now();

        entry.setUser(mockUser);
        entry.setIpAddress("8.8.8.8");
        entry.setUserAgent("UA");
        entry.setDeviceName("Phone");
        entry.setDeviceOs("Android");
        entry.setDeviceFingerprint("fp");
        entry.setGeoCity("City");
        entry.setGeoCountry("Country");
        entry.setSuccess(true);
        entry.setFailureReason("None");
        entry.setTotalSuccessfulLogins(10);
        entry.setLoginAt(now);
        entry.setLogoutAt(now);

        assertEquals(mockUser, entry.getUser());
        assertEquals("8.8.8.8", entry.getIpAddress());
        assertEquals("UA", entry.getUserAgent());
        assertEquals("Phone", entry.getDeviceName());
        assertEquals("Android", entry.getDeviceOs());
        assertEquals("fp", entry.getDeviceFingerprint());
        assertEquals("City", entry.getGeoCity());
        assertEquals("Country", entry.getGeoCountry());
        assertTrue(entry.isSuccess());
        assertEquals("None", entry.getFailureReason());
        assertEquals(10, entry.getTotalSuccessfulLogins());
        assertEquals(now, entry.getLoginAt());
        assertEquals(now, entry.getLogoutAt());
    }
}
