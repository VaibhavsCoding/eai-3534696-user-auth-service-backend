package com.user.authentication.service;

import com.user.authentication.help.UserDetailsHelper;
import com.user.authentication.model.OtpToken;
import com.user.authentication.model.PasswordResetToken;
import com.user.authentication.model.User;
import com.user.authentication.repository.OtpTokenRepository;
import com.user.authentication.repository.PasswordResetTokenRepository;
import com.user.authentication.repository.UserRepository;
import com.user.authentication.security.JwtUtil;
import com.user.authentication.util.GeoLocationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpTokenRepository otpTokenRepository;
    @Mock private PasswordResetTokenRepository resetTokenRepository;
    @Mock private EmailService emailService;
    @Mock private GeoLocationService geoService;
    @Mock private LoginHistoryService loginHistoryService;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserDetailsHelper userDetailsHelper;

    @InjectMocks private UserService userService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        // Use reflection to inject encoder if needed
    }

    // --------------------------------------
    // ✅ LOGIN TEST
    // --------------------------------------
    @Test
    void loginUser_successful() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");
        when(userDetailsHelper.extractClientIp(request)).thenReturn("127.0.0.1");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode("pass"));
        user.setVerified(true);

        // 🔥 REQUIRED FIELDS (Map.of does NOT allow nulls)
        user.setName("Test User");
        user.setDeviceName("Chrome");
        user.setGeoCity("Delhi");
        user.setGeoCountry("India");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(eq("user@example.com"), any()))
                .thenReturn("jwt-token");
        when(loginHistoryService.countSuccessfulLogins(user))
                .thenReturn(1L);

        Map<String, Object> resp =
                userService.loginUser("user@example.com", "pass", request);

        assertTrue((Boolean) resp.get("success"));
        assertEquals("jwt-token", resp.get("token"));
        assertEquals(1, resp.get("totalLogins"));

        verify(loginHistoryService)
                .recordLogin(user, true, null);
    }

    @Test
    void loginUser_invalidPassword() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("correct"));
        user.setVerified(true);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        HttpServletRequest request = mock(HttpServletRequest.class);

        Map<String, Object> resp = userService.loginUser("user@example.com", "wrong", request);

        assertFalse((Boolean) resp.get("success"));
        assertEquals("Invalid password. Try again.", resp.get("message"));
        verify(loginHistoryService).recordLogin(user, false, "Invalid password");
    }

    @Test
    void loginUser_accountLocked() {
        User user = new User();
        user.setEmail("locked@example.com");
        user.setLockUntil(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByEmail("locked@example.com"))
                .thenReturn(Optional.of(user));

        HttpServletRequest request = mock(HttpServletRequest.class);

        Map<String, Object> resp =
                userService.loginUser("locked@example.com", "any", request);

        assertTrue((Boolean) resp.get("locked"));
        assertTrue(resp.containsKey("remainingSeconds"));
        assertTrue(((Long) resp.get("remainingSeconds")) > 0);

        // ✅ VERIFY void method call
        verify(loginHistoryService)
                .recordLogin(user, false, "Account locked");
    }

    // --------------------------------------
    // ✅ OTP TESTS
    // --------------------------------------
    @Test
    void verifyOtp_success() {
        OtpToken token = new OtpToken();
        token.setEmail("test@example.com");
        token.setOtp("123456");
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        User user = new User();
        user.setEmail("test@example.com");

        when(otpTokenRepository.findTopByEmailOrderByCreatedAtDesc("test@example.com"))
                .thenReturn(Optional.of(token));
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        boolean result = userService.verifyOtp("test@example.com", "123456");

        assertTrue(result);

        // ✅ Correct validations
        assertTrue(token.isUsed());
        assertNotNull(user.getLastOtpVerifiedAt());

        verify(otpTokenRepository).save(token);
        verify(userRepository).save(user);
    }

    @Test
    void verifyOtp_failure() {
        OtpToken token = new OtpToken();
        token.setOtp("123456");
        token.setUsed(true); // already used
        token.setExpiresAt(LocalDateTime.now().plusMinutes(1));

        when(otpTokenRepository.findTopByEmailOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(Optional.of(token));

        assertFalse(userService.verifyOtp("user@example.com", "123456"));
    }

    @Test
    void resendOtp_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        userService.resendOtp("test@example.com");

        verify(otpTokenRepository).deleteByEmail("test@example.com");
        verify(otpTokenRepository).save(any());
        verify(emailService).sendOtp(eq("test@example.com"), anyString(), eq("Test User"));
    }

    // --------------------------------------
    // ✅ PASSWORD RESET
    // --------------------------------------
    @Test
    void resetPassword_success() {
        User user = new User();
        user.setEmail("user@example.com");

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("reset123");
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setUsed(false);
        token.setEmail("user@example.com");

        when(resetTokenRepository.findByToken("reset123")).thenReturn(Optional.of(token));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        Map<String, Object> resp = userService.resetPassword("reset123", "newpass");

        assertTrue((Boolean) resp.get("success"));
        assertTrue(passwordEncoder.matches("newpass", user.getPassword()));
        assertTrue(user.getLastPasswordChangedAt() != null);
    }

    @Test
    void resetPassword_expiredToken() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("expired");
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        token.setUsed(false);
        token.setEmail("user@example.com");

        when(resetTokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        Map<String, Object> resp = userService.resetPassword("expired", "any");

        assertFalse((Boolean) resp.get("success"));
        assertEquals("Expired token.", resp.get("message"));
    }

    // --------------------------------------
    // ✅ GEO LOCATION
    // --------------------------------------
    @Test
    void updateGeoLocation_success() {
        User user = new User();
        user.setEmail("geo@example.com");
        user.setIpAddress("127.0.0.1");

        Map<String, String> geo = Map.of("city", "TestCity", "country", "TestCountry");
        when(geoService.getGeoLocation("127.0.0.1")).thenReturn(geo);

        userService.updateGeoLocation(user);

        assertEquals("TestCity", user.getGeoCity());
        assertEquals("TestCountry", user.getGeoCountry());
        verify(userRepository).save(user);
    }

    @Test
    void updateGeoLocation_missingIp() {
        User user = new User();
        user.setEmail("geo@example.com");

        userService.updateGeoLocation(user); // should not throw
        verify(userRepository, never()).save(any());
    }
}
