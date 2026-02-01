package com.user.authentication.service;

import com.user.authentication.model.SignupHistory;
import com.user.authentication.model.User;
import com.user.authentication.repository.SignupHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignupHistoryServiceTest {

    private SignupHistoryRepository signupHistoryRepository;
    private SignupHistoryService signupHistoryService;

    @BeforeEach
    void setUp() {
        signupHistoryRepository = mock(SignupHistoryRepository.class);
        signupHistoryService = new SignupHistoryService(signupHistoryRepository);
    }

    @Test
    void recordSignup_savesSignupHistoryCorrectly() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setIpAddress("192.168.1.1");
        user.setDeviceName("Laptop");
        user.setDeviceOs("Windows 10");
        user.setDeviceFingerprint("fingerprint-123");
        user.setGeoCity("New York");
        user.setGeoCountry("USA");
        user.setUserAgent("Mozilla/5.0");
        user.setSessionId("session-abc");
        user.setVerified(true);

        ArgumentCaptor<SignupHistory> captor = ArgumentCaptor.forClass(SignupHistory.class);

        // Act
        signupHistoryService.recordSignup(user);

        // Assert
        verify(signupHistoryRepository, times(1)).save(captor.capture());

        SignupHistory saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(user.getEmail(), saved.getEmail());
        assertEquals(user.getIpAddress(), saved.getIpAddress());
        assertEquals(user.getDeviceName(), saved.getDeviceName());
        assertEquals(user.getDeviceOs(), saved.getDeviceOs());
        assertEquals(user.getDeviceFingerprint(), saved.getDeviceFingerprint());
        assertEquals(user.getGeoCity(), saved.getGeoCity());
        assertEquals(user.getGeoCountry(), saved.getGeoCountry());
        assertEquals(user.getUserAgent(), saved.getUserAgent());
        assertEquals(user.getSessionId(), saved.getSessionId());
        assertEquals(user.isVerified(), saved.isVerified());
        assertNotNull(saved.getSignupTime());
        assertTrue(saved.getSignupTime().isBefore(LocalDateTime.now().plusSeconds(1))); // sanity check
    }
}
