package com.user.authentication.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {

    @Test
    void testGettersAndSetters() {
        PasswordResetToken token = new PasswordResetToken();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusHours(2);

        token.setId(1L);
        token.setEmail("user@example.com");
        token.setToken("reset-token-123");
        token.setCreatedAt(now);
        token.setExpiresAt(expires);
        token.setUsed(true);

        assertEquals(1L, token.getId());
        assertEquals("user@example.com", token.getEmail());
        assertEquals("reset-token-123", token.getToken());
        assertEquals(now, token.getCreatedAt());
        assertEquals(expires, token.getExpiresAt());
        assertTrue(token.isUsed());
    }

    @Test
    void testIsExpired_whenExpired() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertTrue(token.isExpired());
    }

    @Test
    void testIsExpired_whenNotExpired() {
        PasswordResetToken token = new PasswordResetToken();
        token.setExpiresAt(LocalDateTime.now().plusMinutes(1));

        assertFalse(token.isExpired());
    }

    @Test
    void testIsActive_whenUsed() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUsed(true);

        assertFalse(token.isActive());
    }

    @Test
    void testIsActive_whenExpired() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertFalse(token.isActive());
    }

    @Test
    void testIsActive_whenValid() {
        PasswordResetToken token = new PasswordResetToken();
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(1));

        assertTrue(token.isActive());
    }
}
