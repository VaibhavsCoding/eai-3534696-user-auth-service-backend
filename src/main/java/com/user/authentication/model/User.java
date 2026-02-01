package com.user.authentication.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 🧩 User Entity — Production-Grade
 * Includes account info, device metadata, geo tracking, and audit fields.
 */
@Data
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_mobile", columnList = "mobile")
})
public class User {

    // -------------------------------------------------------------------------
    // 👤 BASIC USER INFO
    // -------------------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String mobile;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean verified = false;

    // -------------------------------------------------------------------------
    // 🌍 DEVICE + NETWORK METADATA
    // -------------------------------------------------------------------------
    /** Client’s public IP as seen by the backend */
    @Column(name = "device_ip", length = 45)
    private String ipAddress;

    /** Browser/app user agent string */
    @Column(length = 512)
    private String userAgent;

    /** OS, device, or platform name if parsed */
    @Column(name = "device_details")
    private String deviceName;

    /** Optional browser/OS version */
    @Column(name = "device_info")
    private String deviceOs;

    /** Optional hashed device fingerprint for recognizing same device */
    @Column
    private String deviceFingerprint;

    /** Optional geo-location info derived from IP (city, country) */
    @Column(length = 100)
    private String geoCity;

    @Column(length = 100)
    private String geoCountry;

    // -------------------------------------------------------------------------
    // 🔐 SECURITY, LOGIN, AND SESSION TRACKING
    // -------------------------------------------------------------------------
    /** Track latest login IP */
    @Column(length = 45)
    private String lastLoginIp;

    /** Track last login time */
    @Column
    private LocalDateTime lastLoginAt;

    /** Session or JWT token ID for traceability */
    @Column(length = 255)
    private String sessionId;

    /** Reason for last failure (invalid password, locked, unverified, etc.) */
    @Column(length = 255)
    private String lastFailureReason;

    /** Track failed login attempts */
    @Column
    private int failedAttempts = 0;

    /** Account locked until this time */
    @Column
    private LocalDateTime lockUntil;

    /** Track OTP/2FA events */
    @Column
    private int otpSentCount = 0;

    @Column
    private int otpFailedCount = 0;

    @Column
    private LocalDateTime lastOtpSentAt;

    @Column
    private LocalDateTime lastOtpVerifiedAt;

    // -------------------------------------------------------------------------
    // 🔄 PASSWORD & ACCOUNT AUDIT
    // -------------------------------------------------------------------------
    @Column
    private LocalDateTime lastPasswordChangedAt;

    @Column
    private LocalDateTime lastPasswordResetAt;

    // -------------------------------------------------------------------------
    // 🧠 ADDITIONAL SECURITY INSIGHTS
    // -------------------------------------------------------------------------
    /** Optional unique ID to trace password change or reset requests */
    @Column(length = 255)
    private String passwordChangeTokenId;

    /** Indicates if the user enabled 2FA manually */
    @Column
    private boolean twoFactorEnabled = false;

    /** Optional field to store source of registration (Web, Mobile, API) */
    @Column(length = 50)
    private String registrationSource;

    // -------------------------------------------------------------------------
    // 🕒 SYSTEM TIMESTAMPS
    // -------------------------------------------------------------------------
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // -------------------------------------------------------------------------
    // ⚙️ HELPER METHODS
    // -------------------------------------------------------------------------
    public boolean isAccountLocked() {
        return lockUntil != null && lockUntil.isAfter(LocalDateTime.now());
    }

    public void incrementFailedAttempts(String reason) {
        this.failedAttempts++;
        this.lastFailureReason = reason;
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lastFailureReason = null;
    }

    public void lockAccountForMinutes(int minutes, String reason) {
        this.lockUntil = LocalDateTime.now().plusMinutes(minutes);
        this.lastFailureReason = reason;
    }

    public void unlockAccount() {
        this.lockUntil = null;
        this.failedAttempts = 0;
        this.lastFailureReason = null;
    }

    public void markPasswordChanged() {
        this.lastPasswordChangedAt = LocalDateTime.now();
    }

    public void markOtpSent() {
        this.otpSentCount++;
        this.lastOtpSentAt = LocalDateTime.now();
    }

    public void markOtpVerified() {
        this.lastOtpVerifiedAt = LocalDateTime.now();
        this.otpFailedCount = 0;
    }

    public void markOtpFailed() {
        this.otpFailedCount++;
    }
}
