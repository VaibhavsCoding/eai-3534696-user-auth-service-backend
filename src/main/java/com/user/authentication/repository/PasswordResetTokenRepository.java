package com.user.authentication.repository;

import com.user.authentication.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // 🔹 Find a token when user clicks on password reset link
    Optional<PasswordResetToken> findByToken(String token);

    // 🔹 Remove all tokens associated with an email (after successful reset)
    @Transactional
    void deleteByEmail(String email);

    // 🔹 Efficiently remove expired tokens (used by the scheduler)
    @Transactional
    int deleteByExpiresAtBefore(LocalDateTime time);
}
