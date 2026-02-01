package com.user.authentication.service;

import com.user.authentication.model.LoginHistory;
import com.user.authentication.model.User;
import com.user.authentication.repository.LoginHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginHistoryServiceTest {

    private LoginHistoryRepository loginHistoryRepository;
    private LoginHistoryService loginHistoryService;

    private User user;

    @BeforeEach
    void setUp() {
        loginHistoryRepository = mock(LoginHistoryRepository.class);
        loginHistoryService = new LoginHistoryService(loginHistoryRepository);
        loginHistoryService.maxLoginHistory = 3; // Set max login history for trimming tests

        user = new User();
        user.setEmail("user@example.com");
    }

    // -------------------- countSuccessfulLogins --------------------
    @Test
    void countSuccessfulLogins_returnsZeroWhenNoHistory() {
        when(loginHistoryRepository.findTopByUserAndSuccessTrueOrderByLoginAtDesc(user))
                .thenReturn(Optional.empty());

        long count = loginHistoryService.countSuccessfulLogins(user);
        assertEquals(0, count);
    }

    @Test
    void countSuccessfulLogins_returnsPreviousTotal() {
        LoginHistory history = new LoginHistory();
        history.setTotalSuccessfulLogins(5);

        when(loginHistoryRepository.findTopByUserAndSuccessTrueOrderByLoginAtDesc(user))
                .thenReturn(Optional.of(history));

        long count = loginHistoryService.countSuccessfulLogins(user);
        assertEquals(5, count);
    }

    @Test
    void countSuccessfulLogins_returnsZeroOnRepositoryException() {
        when(loginHistoryRepository.findTopByUserAndSuccessTrueOrderByLoginAtDesc(user))
                .thenThrow(new RuntimeException("DB failure"));

        long count = loginHistoryService.countSuccessfulLogins(user);

        assertEquals(0, count); // fallback path
    }

    // -------------------- recordLogin --------------------
    @Test
    void recordLogin_successfulLogin_savesEntryAndTrims() {
        LoginHistory lastLogin = new LoginHistory();
        lastLogin.setTotalSuccessfulLogins(2);
        when(loginHistoryRepository.findTopByUserAndSuccessTrueOrderByLoginAtDesc(user))
                .thenReturn(Optional.of(lastLogin));

        // Mock existing history for trimming (5 entries, max = 3)
        List<LoginHistory> historyList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LoginHistory h = new LoginHistory();
            h.setLoginAt(LocalDateTime.now().minusDays(i));
            h.setUser(user);
            historyList.add(h);
        }
        when(loginHistoryRepository.findByUserOrderByLoginAtDesc(user)).thenReturn(historyList);

        loginHistoryService.recordLogin(user, true, null);

        // Verify save called once
        verify(loginHistoryRepository, times(1)).save(any(LoginHistory.class));

        // Verify deleteAll called with trimmed entries
        ArgumentCaptor<List<LoginHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(loginHistoryRepository, times(1)).deleteAll(captor.capture());

        List<LoginHistory> deleted = captor.getValue();
        assertEquals(2, deleted.size()); // 5 entries - keep max 3 = delete 2
    }

    @Test
    void recordLogin_failedLogin_savesEntryWithoutIncrementingSuccess() {
        when(loginHistoryRepository.findTopByUserAndSuccessTrueOrderByLoginAtDesc(user))
                .thenReturn(Optional.of(new LoginHistory()));

        loginHistoryService.recordLogin(user, false, "Wrong password");

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());

        LoginHistory saved = captor.getValue();
        assertFalse(saved.isSuccess());
        assertEquals("Wrong password", saved.getFailureReason());
    }

    @Test
    void recordLogin_handlesExceptionGracefully() {
        when(loginHistoryRepository.findTopByUserAndSuccessTrueOrderByLoginAtDesc(user))
                .thenReturn(Optional.of(new LoginHistory()));
        doThrow(new RuntimeException("DB down")).when(loginHistoryRepository).save(any());

        assertDoesNotThrow(() -> loginHistoryService.recordLogin(user, true, null));
    }

    // -------------------- recordLogout --------------------
    @Test
    void recordLogout_updatesLastLogin() {
        LoginHistory lastLogin = new LoginHistory();
        lastLogin.setSuccess(true);

        when(loginHistoryRepository.findTopByUserAndSuccessTrueOrderByLoginAtDesc(user))
                .thenReturn(Optional.of(lastLogin));

        loginHistoryService.recordLogout(user, "User logged out");

        assertNotNull(lastLogin.getLogoutAt());
        assertEquals("User logged out", lastLogin.getFailureReason());
        verify(loginHistoryRepository).save(lastLogin);
    }

    @Test
    void recordLogout_createsFallbackWhenNoHistory() {
        when(loginHistoryRepository.findTopByUserAndSuccessTrueOrderByLoginAtDesc(user))
                .thenReturn(Optional.empty());

        loginHistoryService.recordLogout(user, "Manual logout");

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());

        LoginHistory saved = captor.getValue();
        assertEquals("Manual logout", saved.getFailureReason());
    }

    @Test
    void recordLogout_handlesExceptionGracefully() {
        when(loginHistoryRepository.findTopByUserAndSuccessTrueOrderByLoginAtDesc(user))
                .thenReturn(Optional.empty());
        doThrow(new RuntimeException("DB down")).when(loginHistoryRepository).save(any());

        assertDoesNotThrow(() -> loginHistoryService.recordLogout(user, "Logout test"));
    }

    // -------------------- trimOldEntries --------------------
    @Test
    void trimOldEntries_keepsOnlyMaxEntries() {
        List<LoginHistory> allEntries = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LoginHistory h = new LoginHistory();
            h.setLoginAt(LocalDateTime.now().minusDays(i));
            h.setUser(user);
            allEntries.add(h);
        }
        when(loginHistoryRepository.findByUserOrderByLoginAtDesc(user)).thenReturn(allEntries);

        loginHistoryService.trimOldEntries(user);

        ArgumentCaptor<List<LoginHistory>> captor = ArgumentCaptor.forClass(List.class);
        verify(loginHistoryRepository, times(1)).deleteAll(captor.capture());

        List<LoginHistory> deleted = captor.getValue();
        assertEquals(2, deleted.size()); // Keep 3, delete 2
    }

    @Test
    void trimOldEntries_exactMaxEntries_doesNotDelete() {
        List<LoginHistory> allEntries = List.of(
                new LoginHistory(), new LoginHistory(), new LoginHistory()
        );
        loginHistoryService.maxLoginHistory = 3;

        when(loginHistoryRepository.findByUserOrderByLoginAtDesc(user)).thenReturn(allEntries);

        loginHistoryService.trimOldEntries(user);

        verify(loginHistoryRepository, never()).deleteAll(anyList());
    }
}
