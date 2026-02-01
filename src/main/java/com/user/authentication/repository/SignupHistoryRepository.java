package com.user.authentication.repository;

import com.user.authentication.model.SignupHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignupHistoryRepository extends JpaRepository<SignupHistory, Long> {
}
