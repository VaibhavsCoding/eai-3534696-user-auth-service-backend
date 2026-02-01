package com.user.authentication.service;

import com.user.authentication.model.SignupHistory;
import com.user.authentication.model.User;
import com.user.authentication.repository.SignupHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SignupHistoryService {

    private final SignupHistoryRepository signupHistoryRepository;

    public void recordSignup(User user) {
        SignupHistory history = SignupHistory.builder()
                .user(user)
                .email(user.getEmail())
                .ipAddress(user.getIpAddress())
                .deviceName(user.getDeviceName())
                .deviceOs(user.getDeviceOs())
                .deviceFingerprint(user.getDeviceFingerprint())
                .geoCity(user.getGeoCity())
                .geoCountry(user.getGeoCountry())
                .userAgent(user.getUserAgent())
                .sessionId(user.getSessionId())
                .signupTime(LocalDateTime.now())
                .verified(user.isVerified())
                .build();

        signupHistoryRepository.save(history);
    }
}
