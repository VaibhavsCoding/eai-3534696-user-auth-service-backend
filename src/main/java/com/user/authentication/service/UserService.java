package com.user.authentication.service;

import com.user.authentication.help.UserDetailsHelper;
import com.user.authentication.model.OtpToken;
import com.user.authentication.model.PasswordResetToken;
import com.user.authentication.model.User;
import com.user.authentication.repository.OtpTokenRepository;
import com.user.authentication.repository.PasswordResetTokenRepository;
import com.user.authentication.repository.UserRepository;
import com.user.authentication.security.JwtUtil;
import com.user.authentication.util.DeviceFingerprintUtil;
import com.user.authentication.util.GeoLocationService;
import com.user.authentication.util.OtpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OtpTokenRepository otpTokenRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final GeoLocationService geoService; // 🌍 Optional IP -> Geo lookup
    private final LoginHistoryService loginHistoryService;
    private final JwtUtil jwtUtil;
    private final UserDetailsHelper userDetailsHelper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final Logger logger = Logger.getLogger(UserService.class.getName());


    // -------------------------------------------------------------------------
    // 📝 REGISTER + OTP
    // -------------------------------------------------------------------------
    @Transactional
    public void saveUser(User user, HttpServletRequest request) {
        logger.info("Attempting to register new user: " + user.getEmail());

        String ip = userDetailsHelper.extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        user.setIpAddress(ip);
        user.setUserAgent(userAgent);
        user.setDeviceFingerprint(DeviceFingerprintUtil.generateFingerprint(userAgent));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setLastPasswordChangedAt(LocalDateTime.now());

        try {
            if (geoService != null) {
                Map<String, String> geoData = geoService.getGeoLocation(ip);
                if (geoData != null) {
                    user.setGeoCity(geoData.get("city"));
                    user.setGeoCountry(geoData.get("country"));
                }
            }
        } catch (Exception e) {
            logger.warning("Geo lookup failed: " + e.getMessage());
        }

        userRepository.save(user);
        logger.info("User saved successfully: " + user.getEmail());

        // 🔢 Generate and store OTP
        String otp = OtpUtil.generateOtp(6);
        OtpToken otpToken = otpTokenRepository.findByEmail(user.getEmail())
                .orElseGet(OtpToken::new);

        otpToken.setEmail(user.getEmail());
        otpToken.setOtp(otp);
        otpToken.setCreatedAt(LocalDateTime.now());
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(2));
        otpToken.setUsed(false);
        otpTokenRepository.save(otpToken);

        user.markOtpSent();
        userRepository.save(user);

        emailService.sendOtp(user.getEmail(), otp, user.getName());
        logger.info("OTP sent to: " + user.getEmail());
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // -------------------------------------------------------------------------
    // 🧠 LOGIN
    // -------------------------------------------------------------------------
    @Transactional
    public Map<String, Object> loginUser(String email, String password, HttpServletRequest request) {
        Map<String, Object> resp = new HashMap<>();
        logger.info("🔐 Login attempt for " + email);

        try {
            // ✅ Basic validation
            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                throw new IllegalArgumentException("Email and password are required.");
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("Invalid credentials.");
            }

            User user = userOpt.get();

            // 🔒 Account lock check
            if (user.isAccountLocked()) {
                long remainingSeconds = Duration.between(LocalDateTime.now(), user.getLockUntil()).getSeconds();
                resp.put("locked", true);
                resp.put("remainingSeconds", remainingSeconds);
                resp.put("message", "Account locked. Try again in " + remainingSeconds + " seconds.");
                loginHistoryService.recordLogin(user, false, "Account locked");
                return resp;
            }

            // 🔑 Password check
            if (!passwordEncoder.matches(password, user.getPassword())) {
                user.incrementFailedAttempts("Invalid password");

                if (user.getFailedAttempts() >= 3) {
                    user.lockAccountForMinutes(30, "Too many failed attempts");
                    userRepository.save(user);
                    loginHistoryService.recordLogin(user, false, "Too many failed attempts");

                    resp.put("locked", true);
                    resp.put("remainingSeconds", 1800);
                    resp.put("message", "Account locked for 30 minutes due to 3 failed attempts.");
                    return resp;
                }

                userRepository.save(user);
                loginHistoryService.recordLogin(user, false, "Invalid password");
                resp.put("success", false);
                resp.put("message", "Invalid password. Try again.");
                return resp;
            }

            // 🔍 Unverified user check
            if (!user.isVerified()) {
                loginHistoryService.recordLogin(user, false, "Unverified account");
                resp.put("success", false);
                resp.put("unverified", true);
                resp.put("message", "Please verify your email before logging in.");
                return resp;
            }

            // 🌍 Use geo data from frontend payload (no server-side fetch needed)
            String userAgent = request.getHeader("User-Agent");
            String clientIp = userDetailsHelper.extractClientIp(request);
            String deviceType = DeviceFingerprintUtil.getDeviceType(userAgent);

            // Extract geo from request body (sent by frontend)
            // Assuming your controller parses the JSON payload into a Map or DTO
            // For simplicity, I'll assume you have access to the payload fields here
            // (In a real app, pass them as method params or extract from request)
            String geoCity = "Unknown";  // Default
            String geoCountry = "Unknown";  // Default
            // TODO: Extract from request body, e.g., via @RequestBody Map<String, Object> payload
            // geoCity = (String) payload.get("geoCity");
            // geoCountry = (String) payload.get("geoCountry");

            // ✅ Successful login updates
            user.resetFailedAttempts();
            user.setLastFailureReason(null);
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(clientIp);
            user.setUserAgent(userAgent);
            user.setDeviceName(deviceType);
            user.setDeviceOs(deviceType);
            user.setDeviceFingerprint(DeviceFingerprintUtil.generateFingerprint(userAgent));
            user.setGeoCity(geoCity);
            user.setGeoCountry(geoCountry);
            user.setSessionId(UUID.randomUUID().toString());
            userRepository.save(user);

            // 🧾 Record successful login
            loginHistoryService.recordLogin(user, true, null);

            // 🔑 Generate JWT token
            String token = jwtUtil.generateToken(email, null);

            // 🧮 Count total successful logins
            int successfulLogins = (int) loginHistoryService.countSuccessfulLogins(user);

            // 🧱 Build response
            resp.put("success", true);
            resp.put("token", token);
            resp.put("totalLogins", successfulLogins);
            resp.put("message", "Login successful!");
            resp.put("user", Map.of(
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "sessionId", user.getSessionId(),
                    "ipAddress", user.getLastLoginIp(),
                    "device", user.getDeviceName(),
                    "city", user.getGeoCity(),
                    "country", user.getGeoCountry()
            ));

            logger.info("✅ Login success for " + email);
            return resp;

        } catch (IllegalArgumentException e) {
            logger.warning("⚠️ Login failed for " + email + " — " + e.getMessage());
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return resp;
        } catch (Exception e) {
            logger.severe("💥 Unexpected error during login for " + email + ": " + e.getMessage());
            resp.put("success", false);
            resp.put("message", "An unexpected error occurred. Please try again later.");
            return resp;
        }
    }


    // -------------------------------------------------------------------------
    // 🔐 OTP VERIFY / RESEND
    // -------------------------------------------------------------------------
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        Optional<OtpToken> otpOpt = otpTokenRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (otpOpt.isEmpty()) return false;

        OtpToken token = otpOpt.get();

        if (token.isUsed() || token.getExpiresAt().isBefore(LocalDateTime.now()) || !token.getOtp().equals(otp)) {
            otpOpt.ifPresent(t -> {
                t.setUsed(true);
                otpTokenRepository.save(t);
            });
            return false;
        }

        token.setUsed(true);
        otpTokenRepository.save(token);

        userRepository.findByEmail(email).ifPresent(u -> {
            u.setVerified(true);
            u.markOtpVerified();
            userRepository.save(u);
        });

        return true;
    }

    @Transactional
    public void resendOtp(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();
        otpTokenRepository.deleteByEmail(email);

        String otp = OtpUtil.generateOtp(6);
        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(email);
        otpToken.setOtp(otp);
        otpToken.setCreatedAt(LocalDateTime.now());
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(2));
        otpToken.setUsed(false);
        otpTokenRepository.save(otpToken);

        user.markOtpSent();
        userRepository.save(user);

        emailService.sendOtp(email, otp, user.getName());
        logger.info("Resent OTP to: " + email);
    }

    // -------------------------------------------------------------------------
    // 🌍 GEO LOCATION UPDATE
    // -------------------------------------------------------------------------
    public void updateGeoLocation(User user) {
        if (user.getIpAddress() == null || user.getIpAddress().isEmpty()) {
            logger.warning("Cannot update geo location: missing IP for user " + user.getEmail());
            return;
        }

        try {
            Map<String, String> geoData = geoService.getGeoLocation(user.getIpAddress());
            if (geoData != null) {
                user.setGeoCity(geoData.get("city"));
                user.setGeoCountry(geoData.get("country"));
                userRepository.save(user);
                logger.info("✅ Geo location updated for user: " + user.getEmail());
            } else {
                logger.warning("No geo data found for IP: " + user.getIpAddress());
            }
        } catch (Exception e) {
            logger.severe("🌍 Failed to update geo location for user " + user.getEmail() + ": " + e.getMessage());
        }
    }


    // -------------------------------------------------------------------------
    // 🔄 PASSWORD RESET
    // -------------------------------------------------------------------------
    @Transactional
    public String generatePasswordResetToken(String email) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);
        resetTokenRepository.save(resetToken);
        return token;
    }

    public void sendPasswordResetEmail(String email,String name, String resentLink) {


        emailService.sendPasswordResetEmail(email, name, resentLink);
    }

    @Transactional
    public Map<String, Object> resetPassword(String token, String newPassword) {
        Map<String, Object> response = new HashMap<>();
        Optional<PasswordResetToken> tokenOpt = resetTokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invalid token.");
            return response;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetTokenRepository.deleteByEmail(resetToken.getEmail());
            response.put("success", false);
            response.put("message", "Expired token.");
            return response;
        }

        Optional<User> userOpt = userRepository.findByEmail(resetToken.getEmail());
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found.");
            return response;
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.markPasswordChanged();
        userRepository.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        response.put("success", true);
        response.put("message", "Password reset successfully.");
        return response;
    }
}
