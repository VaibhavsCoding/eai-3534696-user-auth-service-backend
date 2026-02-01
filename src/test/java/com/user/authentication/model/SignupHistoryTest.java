package com.user.authentication.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SignupHistoryTest {

    @Test
    void testGettersAndSetters() {
        SignupHistory history = new SignupHistory();
        User mockUser = new User();
        LocalDateTime now = LocalDateTime.now();

        history.setId(1L);
        history.setUser(mockUser);
        history.setEmail("test@example.com");
        history.setIpAddress("127.0.0.1");
        history.setDeviceName("Phone");
        history.setDeviceOs("Android");
        history.setDeviceFingerprint("fp123");
        history.setGeoCity("Berlin");
        history.setGeoCountry("Germany");
        history.setUserAgent("Mozilla/5.0");
        history.setSessionId("sess123");
        history.setSignupTime(now);
        history.setVerified(true);

        assertEquals(1L, history.getId());
        assertEquals(mockUser, history.getUser());
        assertEquals("test@example.com", history.getEmail());
        assertEquals("127.0.0.1", history.getIpAddress());
        assertEquals("Phone", history.getDeviceName());
        assertEquals("Android", history.getDeviceOs());
        assertEquals("fp123", history.getDeviceFingerprint());
        assertEquals("Berlin", history.getGeoCity());
        assertEquals("Germany", history.getGeoCountry());
        assertEquals("Mozilla/5.0", history.getUserAgent());
        assertEquals("sess123", history.getSessionId());
        assertEquals(now, history.getSignupTime());
        assertTrue(history.isVerified());
    }

    @Test
    void testBuilder() {
        User mockUser = new User();
        LocalDateTime now = LocalDateTime.now();

        SignupHistory history = SignupHistory.builder()
                .id(2L)
                .user(mockUser)
                .email("builder@example.com")
                .ipAddress("192.168.0.1")
                .deviceName("Laptop")
                .deviceOs("Windows")
                .deviceFingerprint("fp456")
                .geoCity("Paris")
                .geoCountry("France")
                .userAgent("Chrome/90")
                .sessionId("sess456")
                .signupTime(now)
                .verified(false)
                .build();

        assertEquals(2L, history.getId());
        assertEquals(mockUser, history.getUser());
        assertEquals("builder@example.com", history.getEmail());
        assertEquals("192.168.0.1", history.getIpAddress());
        assertEquals("Laptop", history.getDeviceName());
        assertEquals("Windows", history.getDeviceOs());
        assertEquals("fp456", history.getDeviceFingerprint());
        assertEquals("Paris", history.getGeoCity());
        assertEquals("France", history.getGeoCountry());
        assertEquals("Chrome/90", history.getUserAgent());
        assertEquals("sess456", history.getSessionId());
        assertEquals(now, history.getSignupTime());
        assertFalse(history.isVerified());
    }
}
