package com.user.authentication.controller;

import com.user.authentication.dto.LoginRequest;
import com.user.authentication.help.UserDetailsHelper;
import com.user.authentication.model.User;
import com.user.authentication.repository.UserRepository;
import com.user.authentication.security.JwtUtil;
import com.user.authentication.service.LoginHistoryService;
import com.user.authentication.service.SignupHistoryService;
import com.user.authentication.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private LoginHistoryService loginHistoryService;
    private SignupHistoryService signupHistoryService;
    private UserDetailsHelper userDetailsHelper;
    private HttpServletRequest request;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userRepository = mock(UserRepository.class);
        jwtUtil = mock(JwtUtil.class);
        loginHistoryService = mock(LoginHistoryService.class);
        signupHistoryService = mock(SignupHistoryService.class);
        userDetailsHelper = mock(UserDetailsHelper.class);
        request = mock(HttpServletRequest.class);

        userController = new UserController(
                userService, userRepository, jwtUtil,
                loginHistoryService, signupHistoryService, userDetailsHelper
        );
    }

    // ---------------- LOGIN ----------------
    @Test
    void login_success_and_failure() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("pass");

        Map<String, Object> successResp = Map.of("success", true);
        Map<String, Object> failResp = Map.of("success", false);

        when(userService.loginUser(anyString(), anyString(), any())).thenReturn(successResp);
        ResponseEntity<Map<String, Object>> resp = userController.login(req, request);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().get("success").equals(true));

        when(userService.loginUser(anyString(), anyString(), any())).thenReturn(failResp);
        resp = userController.login(req, request);
        assertEquals(401, resp.getStatusCodeValue());
        assertFalse(resp.getBody().get("success").equals(true));

        // Locked case
        Map<String, Object> lockedResp = new HashMap<>();
        lockedResp.put("locked", true);
        when(userService.loginUser(anyString(), anyString(), any())).thenReturn(lockedResp);
        resp = userController.login(req, request);
        assertEquals(403, resp.getStatusCodeValue());
    }

    // ---------------- SIGNUP ----------------
    @Test
    void signup_success_and_failure() {
        User user = new User();
        user.setEmail("user@example.com");

        when(userService.emailExists(anyString())).thenReturn(false);
        when(userDetailsHelper.extractClientIp(any())).thenReturn("127.0.0.1");
        when(userDetailsHelper.getDeviceType(any())).thenReturn("Laptop");
        when(userDetailsHelper.fetchGeoData(anyString())).thenReturn(Map.of("city", "NY", "country", "USA"));
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("token123");

        ResponseEntity<Map<String, Object>> resp = userController.signup(user, request);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue((Boolean) resp.getBody().get("success"));

        // Email already exists
        when(userService.emailExists(anyString())).thenReturn(true);
        resp = userController.signup(user, request);
        assertEquals(400, resp.getStatusCodeValue());
        assertFalse((Boolean) resp.getBody().get("success"));

        // Null email
        User user2 = new User();
        resp = userController.signup(user2, request);
        assertEquals(400, resp.getStatusCodeValue());
    }

    // ---------------- VERIFY OTP ----------------
    @Test
    void verifyOtp_tests() {
        Map<String, String> req = Map.of("email", "a@b.com", "otp", "1234");

        when(userService.verifyOtp("a@b.com", "1234")).thenReturn(true);
        ResponseEntity<Map<String, Object>> resp = userController.verifyOtp(req);
        assertTrue((Boolean) resp.getBody().get("success"));

        when(userService.verifyOtp("a@b.com", "1234")).thenReturn(false);
        resp = userController.verifyOtp(req);
        assertFalse((Boolean) resp.getBody().get("success"));

        // Null email
        resp = userController.verifyOtp(Map.of("otp", "1234"));
        assertEquals(400, resp.getStatusCodeValue());
    }

    // ---------------- VERIFY TOKEN ----------------
    @Test
    void verifyToken_tests() {
        Jws<Claims> claims = mock(Jws.class);
        Claims body = mock(Claims.class);
        when(claims.getBody()).thenReturn(body);
        when(body.getSubject()).thenReturn("user@example.com");
        when(body.getIssuedAt()).thenReturn(new Date());
        when(body.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 1000));

        when(jwtUtil.validateToken("valid")).thenReturn(claims);
        ResponseEntity<Map<String, Object>> resp = userController.verifyToken("Bearer valid");
        assertTrue((Boolean) resp.getBody().get("valid"));

        // Missing header
        resp = userController.verifyToken(null);
        assertEquals(401, resp.getStatusCodeValue());

        // Expired token
        when(jwtUtil.validateToken("expired")).thenThrow(ExpiredJwtException.class);
        resp = userController.verifyToken("Bearer expired");
        assertEquals(401, resp.getStatusCodeValue());

        // Invalid token
        when(jwtUtil.validateToken("invalid")).thenThrow(JwtException.class);
        resp = userController.verifyToken("Bearer invalid");
        assertEquals(401, resp.getStatusCodeValue());
    }

    // ---------------- RESEND OTP ----------------
    @Test
    void resendOtp_tests() {
        Map<String, String> req = Map.of("email", "test@example.com");
        ResponseEntity<Map<String, Object>> resp = userController.resendOtp(req);
        assertTrue((Boolean) resp.getBody().get("success"));

        // Blank email
        resp = userController.resendOtp(Map.of("email", ""));
        assertEquals(400, resp.getStatusCodeValue());
    }

    // ---------------- FORGOT PASSWORD ----------------
    @Test
    void forgotPassword_tests() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setName("Tester");

        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(userService.generatePasswordResetToken("a@b.com")).thenReturn("token123");

        ResponseEntity<Map<String, Object>> resp = userController.forgotPassword(Map.of("email", "a@b.com"));
        assertTrue((Boolean) resp.getBody().get("success"));

        // Email not found
        when(userRepository.findByEmail("b@b.com")).thenReturn(Optional.empty());
        resp = userController.forgotPassword(Map.of("email", "b@b.com"));
        assertFalse((Boolean) resp.getBody().get("success"));

        // Null email
        resp = userController.forgotPassword(Map.of());
        assertEquals(400, resp.getStatusCodeValue());
    }

    // ---------------- RESET PASSWORD ----------------
    @Test
    void resetPassword_tests() {
        Map<String, String> req = Map.of("token", "t", "newPassword", "p");
        when(userService.resetPassword("t", "p")).thenReturn(Map.of("success", true));
        ResponseEntity<Map<String, Object>> resp = userController.resetPassword(req);
        assertTrue((Boolean) resp.getBody().get("success"));

        // Missing fields
        resp = userController.resetPassword(Map.of("token", "t"));
        assertEquals(400, resp.getStatusCodeValue());
    }

    // ---------------- LOGOUT ----------------
    @Test
    void logout_tests() {
        when(jwtUtil.getSubject("token")).thenReturn("a@b.com");
        User user = new User();
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        ResponseEntity<Map<String, Object>> resp = userController.logout("Bearer token");
        assertTrue((Boolean) resp.getBody().get("success"));

        // Missing header
        resp = userController.logout(null);
        assertEquals(400, resp.getStatusCodeValue());

        // User not found
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        resp = userController.logout("Bearer token");
        assertEquals(404, resp.getStatusCodeValue());
    }

    // ---------------- GEO ----------------
    @Test
    void getGeo_tests() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        ResponseEntity<Map<String, String>> resp = userController.getGeo(request);
        assertEquals("Localhost", resp.getBody().get("city"));
    }
}
