package com.user.authentication.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 📜 LoginHistory Entity — Tracks each user login attempt (success or failure) and logouts
 * Includes metadata (IP, device, location), timestamps, and success count.
 */
@Data
@Entity
@Table(
        name = "login_history",
        indexes = {
                @Index(name = "idx_login_history_user_id", columnList = "user_id"),
                @Index(name = "idx_login_history_ip", columnList = "ip_address"),
                @Index(name = "idx_login_history_login_at", columnList = "login_at"),
                @Index(name = "idx_login_history_logout_at", columnList = "logout_at")
        }
)
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // 👤 USER RELATIONSHIP
    // -------------------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // -------------------------------------------------------------------------
    // 🌍 DEVICE + NETWORK DETAILS
    // -------------------------------------------------------------------------
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "device_os", length = 100)
    private String deviceOs;

    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;

    @Column(name = "geo_city", length = 100)
    private String geoCity;

    @Column(name = "geo_country", length = 100)
    private String geoCountry;

    // -------------------------------------------------------------------------
    // 🔐 LOGIN STATUS & ATTEMPT INFO
    // -------------------------------------------------------------------------
    /** Whether login succeeded or failed */
    @Column(name = "success", nullable = false)
    private boolean success = true;

    /** Reason for failure (invalid password, locked, etc.) or logout reason */
    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    /** Total successful login count for this user */
    @Column(name = "total_successful_logins", nullable = false)
    private int totalSuccessfulLogins;

    // -------------------------------------------------------------------------
    // 🕒 TIMESTAMPS
    // -------------------------------------------------------------------------
    /** Exact login time (helps separate app-level timestamp from DB insert time) */
    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt = LocalDateTime.now();

    /** Logout time (nullable, only set for logout entries) */
    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    // -------------------------------------------------------------------------
    // ⚙️ FACTORY HELPERS
    // -------------------------------------------------------------------------
    public static LoginHistory successEntry(
            User user,
            int totalSuccessfulLogins,
            String ip,
            String ua,
            String os,
            String deviceName,
            String fingerprint,
            String city,
            String country
    ) {
        LoginHistory entry = new LoginHistory();
        entry.setUser(user);
        entry.setIpAddress(ip);
        entry.setUserAgent(ua);
        entry.setDeviceOs(os);
        entry.setDeviceName(deviceName);
        entry.setDeviceFingerprint(fingerprint);
        entry.setGeoCity(city);
        entry.setGeoCountry(country);
        entry.setSuccess(true);
        entry.setFailureReason(null);
        entry.setTotalSuccessfulLogins(totalSuccessfulLogins);
        entry.setLoginAt(LocalDateTime.now());
        return entry;
    }

    public static LoginHistory failureEntry(
            User user,
            String ip,
            String ua,
            String reason,
            String os,
            String deviceName,
            String fingerprint,
            String city,
            String country,
            int totalSuccessfulLogins
    ) {
        LoginHistory entry = new LoginHistory();
        entry.setUser(user);
        entry.setIpAddress(ip);
        entry.setUserAgent(ua);
        entry.setDeviceOs(os);
        entry.setDeviceName(deviceName);
        entry.setDeviceFingerprint(fingerprint);
        entry.setGeoCity(city);
        entry.setGeoCountry(country);
        entry.setSuccess(false);
        entry.setFailureReason(reason);
        entry.setTotalSuccessfulLogins(totalSuccessfulLogins);
        entry.setLoginAt(LocalDateTime.now());
        return entry;
    }

    public static LoginHistory logoutEntry(
            User user,
            String ip,
            String ua,
            String reason,
            String os,
            String deviceName,
            String fingerprint,
            String city,
            String country
    ) {
        LoginHistory entry = new LoginHistory();
        entry.setUser(user);
        entry.setIpAddress(ip);
        entry.setUserAgent(ua);
        entry.setDeviceOs(os);
        entry.setDeviceName(deviceName);
        entry.setDeviceFingerprint(fingerprint);
        entry.setGeoCity(city);
        entry.setGeoCountry(country);
        entry.setSuccess(false);  // Treat logout as non-success for consistency
        entry.setFailureReason(reason);
        entry.setTotalSuccessfulLogins(0);  // Not applicable for logout
        entry.setLoginAt(LocalDateTime.now());  // Use as session end time
        entry.setLogoutAt(LocalDateTime.now());
        return entry;
    }
}