package com.user.authentication.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OtpTokenTest {

    @Test
    void testGettersAndSetters() {
        OtpToken token = new OtpToken();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusMinutes(5);

        token.setId(1L);
        token.setEmail("test@example.com");
        token.setOtp("123456");
        token.setCreatedAt(now);
        token.setExpiresAt(expires);
        token.setUsed(true);

        assertEquals(1L, token.getId());
        assertEquals("test@example.com", token.getEmail());
        assertEquals("123456", token.getOtp());
        assertEquals(now, token.getCreatedAt());
        assertEquals(expires, token.getExpiresAt());
        assertTrue(token.isUsed());
    }

    @Test
    void testIsExpired_whenExpired() {
        OtpToken token = new OtpToken();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertTrue(token.isExpired());
    }

    @Test
    void testIsExpired_whenNotExpired() {
        OtpToken token = new OtpToken();
        token.setExpiresAt(LocalDateTime.now().plusMinutes(1));

        assertFalse(token.isExpired());
    }

    @Test
    void testIsActive_whenUsed() {
        OtpToken token = new OtpToken();
        token.setUsed(true);

        assertFalse(token.isActive());
    }

    @Test
    void testIsActive_whenExpired() {
        OtpToken token = new OtpToken();
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertFalse(token.isActive());
    }

    @Test
    void testIsActive_whenValid() {
        OtpToken token = new OtpToken();
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(1));

        assertTrue(token.isActive());
    }
}
