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
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // adjust for production
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final LoginHistoryService loginHistoryService;
    private final SignupHistoryService signupHistoryService;
    private final UserDetailsHelper userDetailsHelper;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class.getName());
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // -------------------------------------------------------------
    // 🔹 LOGIN
    // -------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        Map<String, Object> resp = userService.loginUser(req.getEmail(), req.getPassword(), request);

        if (Boolean.TRUE.equals(resp.get("success"))) {
            return ResponseEntity.ok(resp);
        } else if (resp.containsKey("locked")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resp);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
    }

    // -------------------------------------------------------------
    // 🔹 SIGNUP
    // -------------------------------------------------------------
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody User user, HttpServletRequest request) {
        Map<String, Object> resp = new HashMap<>();
        String email = user.getEmail();
        logger.info("📝 Signup request received for email={}", email);

        try {
            if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
            if (userService.emailExists(email)) throw new IllegalStateException("Email already registered");

            // 🌍 Capture user environment info
            String userAgent = request.getHeader("User-Agent");
            String clientIp = userDetailsHelper.extractClientIp(request);
            String deviceType = userDetailsHelper.getDeviceType(userAgent);
            Map<String, String> geoData = userDetailsHelper.fetchGeoData(clientIp);

            user.setUserAgent(userAgent);
            user.setDeviceName(deviceType);
            user.setIpAddress(clientIp);
            user.setGeoCity(geoData.get("city"));
            user.setGeoCountry(geoData.get("country"));
            user.setSessionId(UUID.randomUUID().toString());
            user.setCreatedAt(LocalDateTime.now());
            user.setVerified(false);

            userService.saveUser(user, request);
            signupHistoryService.recordSignup(user);

            logger.info("✅ Signup successful — OTP sent to {}", email);

            String token = jwtUtil.generateToken(email, null);
            resp.put("success", true);
            resp.put("message", "Signup successful! OTP sent to email.");
            resp.put("token", token);

        } catch (Exception e) {
            logger.error("💥 Signup error for {}: {}", email, e.getMessage());
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }

        return ResponseEntity.ok(resp);
    }

    // -------------------------------------------------------------
    // 🔹 VERIFY OTP
    // -------------------------------------------------------------
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        Map<String, Object> resp = new HashMap<>();

        logger.info("🔎 OTP verification attempt for {}", email);

        if (email == null || otp == null) {
            resp.put("success", false);
            resp.put("message", "Email and OTP are required");
            return ResponseEntity.badRequest().body(resp);
        }

        try {
            boolean valid = userService.verifyOtp(email, otp);
            if (valid) {
                logger.info("✅ OTP verified successfully for {}", email);
                resp.put("success", true);
                resp.put("message", "OTP verified successfully");
            } else {
                logger.warn("❌ Invalid or expired OTP for {}", email);
                resp.put("success", false);
                resp.put("message", "Invalid or expired OTP");
            }
        } catch (Exception e) {
            logger.error("💥 OTP verification error for {}: {}", email, e.getMessage());
            resp.put("success", false);
            resp.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }

        return ResponseEntity.ok(resp);
    }

    // -------------------------------------------------------------
    // 🔹 VERIFY TOKEN
    // -------------------------------------------------------------
    @GetMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("valid", false);
                response.put("message", "Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7);
            Jws<Claims> claims = jwtUtil.validateToken(token);

            response.put("valid", true);
            response.put("subject", claims.getBody().getSubject());
            response.put("issuedAt", claims.getBody().getIssuedAt());
            response.put("expiresAt", claims.getBody().getExpiration());
            return ResponseEntity.ok(response);

        } catch (ExpiredJwtException e) {
            response.put("valid", false);
            response.put("message", "Token expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (JwtException e) {
            response.put("valid", false);
            response.put("message", "Invalid token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // -------------------------------------------------------------
    // 🔹 RESEND OTP
    // -------------------------------------------------------------
    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        Map<String, Object> resp = new HashMap<>();
        logger.info("🔁 Resend OTP request for {}", email);

        if (email == null || email.isBlank()) {
            resp.put("success", false);
            resp.put("message", "Email is required");
            return ResponseEntity.badRequest().body(resp);
        }

        try {
            userService.resendOtp(email);
            logger.info("📧 OTP resent successfully to {}", email);
            resp.put("success", true);
            resp.put("message", "OTP resent successfully");
        } catch (Exception e) {
            logger.error("💥 Error resending OTP to {}: {}", email, e.getMessage());
            resp.put("success", false);
            resp.put("message", "Error resending OTP");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }

        return ResponseEntity.ok(resp);
    }

    // -------------------------------------------------------------
    // 🔹 FORGOT PASSWORD
    // -------------------------------------------------------------
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        Map<String, Object> resp = new HashMap<>();
        logger.info("🔑 Forgot password requested for {}", email);

        if (email == null || email.isBlank()) {
            resp.put("success", false);
            resp.put("message", "Email is required");
            return ResponseEntity.badRequest().body(resp);
        }

        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                resp.put("success", false);
                resp.put("message", "Email not found");
                return ResponseEntity.ok(resp);
            }

            User user = userOpt.get();
            String name = user.getName() != null ? user.getName() : "User";

            String token = userService.generatePasswordResetToken(email);
            String resetLink = "http://localhost:3000/reset-password?token=" + token;
            userService.sendPasswordResetEmail(email, name, resetLink);

            resp.put("success", true);
            resp.put("message", "Password reset link sent to your email");
            resp.put("email", email);
            resp.put("name", name);

        } catch (Exception e) {
            logger.error("💥 Error sending password reset link: {}", e.getMessage());
            resp.put("success", false);
            resp.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }

        return ResponseEntity.ok(resp);
    }

    // -------------------------------------------------------------
    // 🔹 RESET PASSWORD
    // -------------------------------------------------------------
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> req) {
        String token = req.get("token");
        String newPassword = req.get("newPassword");
        Map<String, Object> resp = new HashMap<>();

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Token and new password are required");
            return ResponseEntity.badRequest().body(resp);
        }

        try {
            resp = userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("💥 Error resetting password: {}", e.getMessage());
            resp.put("success", false);
            resp.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // -------------------------------------------------------------
    // 🔹 LOGOUT
    // -------------------------------------------------------------
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader) {

        Map<String, Object> resp = new HashMap<>();

        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            resp.put("success", false);
            resp.put("message", "Missing or invalid token.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }

        String token = tokenHeader.substring(7);
        try {
            String email = jwtUtil.getSubject(token);
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                resp.put("success", false);
                resp.put("message", "User not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
            }

            loginHistoryService.recordLogout(userOpt.get(), "User logged out");

            resp.put("success", true);
            resp.put("message", "Logout recorded successfully.");
            logger.info("✅ User logged out successfully: {}", email);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            logger.error("💥 Logout error: {}", e.getMessage(), e);
            resp.put("success", false);
            resp.put("message", "Logout failed due to server error.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // -------------------------------------------------------------
    // 🌍 GEOLOCATION ENDPOINT (Actual IP & Location)
    // -------------------------------------------------------------
    @GetMapping("/geo")
    public ResponseEntity<Map<String, String>> getGeo(HttpServletRequest request) {
        Map<String, String> geoData = new HashMap<>();

        try {
            String ip = request.getRemoteAddr();

            // Localhost fallback
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                geoData.put("ip", ip);
                geoData.put("city", "Localhost");
                geoData.put("region", "Local");
                geoData.put("country", "Local");
                geoData.put("latitude", "0");
                geoData.put("longitude", "0");
                return ResponseEntity.ok(geoData);
            }

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> data = restTemplate.getForObject("https://ipapi.co/json/", Map.class);

            // Populate geoData from API response
            geoData.put("ip", String.valueOf(data.get("ip")));
            geoData.put("city", String.valueOf(data.get("city")));
            geoData.put("region", String.valueOf(data.get("region")));
            geoData.put("country", String.valueOf(data.get("country_name")));
            geoData.put("latitude", String.valueOf(data.get("latitude")));
            geoData.put("longitude", String.valueOf(data.get("longitude")));

        } catch (Exception e) {
            geoData.put("ip", "");
            geoData.put("city", "");
            geoData.put("region", "");
            geoData.put("country", "");
            geoData.put("latitude", "");
            geoData.put("longitude", "");
            System.err.println("🌍 Geo lookup failed: " + e.getMessage());
        }

        return ResponseEntity.ok(geoData);
    }
}
