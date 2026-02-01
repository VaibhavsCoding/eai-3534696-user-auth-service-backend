package com.user.authentication.repository;

import com.user.authentication.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    // 🔹 Get the latest OTP for a given email
    Optional<OtpToken> findTopByEmailOrderByCreatedAtDesc(String email);

    // 🔹 Find OTP by email (used for checking existing before creating new)
    Optional<OtpToken> findByEmail(String email);

    // 🔹 Delete all OTPs associated with a specific email (used after verification)
    @Transactional
    void deleteByEmail(String email);

    // 🔹 Efficient cleanup: delete all expired OTPs in one DB query
    @Transactional
    int deleteByExpiresAtBefore(LocalDateTime expiryTime);
}
