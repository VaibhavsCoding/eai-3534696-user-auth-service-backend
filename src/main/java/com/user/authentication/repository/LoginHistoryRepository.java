package com.user.authentication.repository;

import com.user.authentication.model.LoginHistory;
import com.user.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByUserOrderByLoginAtDesc(User user);

    List<LoginHistory> findTop5ByUserOrderByLoginAtDesc(User user);

    /**
     * Find most recent successful login for the user.
     * Spring Data will implement this derived query.
     */
    Optional<LoginHistory> findTopByUserAndSuccessTrueOrderByLoginAtDesc(User user);

    /**
     * Count successful logins for a user (success = true).
     * Spring Data will implement this derived query.
     */
    long countByUserAndSuccessTrue(User user);
}
