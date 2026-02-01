package com.user.authentication.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Email should NOT be unique — users may request multiple tokens over time.
    @Column(nullable = false, length = 255)
    private String email;

    // 🔹 Token itself must be unique for validation
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

    @Column(nullable = false)
    private boolean used = false;

    // 🔹 Helper methods for logic clarity
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isActive() {
        return !used && !isExpired();
    }
}
