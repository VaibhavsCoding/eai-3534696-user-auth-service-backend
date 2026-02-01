package com.user.authentication.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "otp_tokens")
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Email should NOT be unique — multiple OTPs may be issued sequentially (e.g., resend).
    @Column(nullable = false, length = 255)
    private String email;

    // 🔹 Store OTP as a short string, 4–6 digits typically.
    @Column(nullable = false, length = 10)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(2); // OTP valid for 2 mins

    @Column(nullable = false)
    private boolean used = false;

    // 🔹 Check if OTP is expired
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    // 🔹 Helper for clarity
    public boolean isActive() {
        return !used && !isExpired();
    }
}
