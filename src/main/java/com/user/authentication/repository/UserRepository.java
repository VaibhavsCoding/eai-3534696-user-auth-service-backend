package com.user.authentication.repository;

import com.user.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMobile(String mobile);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.verified = true")
    Optional<User> findVerifiedUserByEmail(String email);
}
