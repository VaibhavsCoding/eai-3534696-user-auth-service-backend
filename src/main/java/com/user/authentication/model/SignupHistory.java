package com.user.authentication.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signup_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🧍 Linked to User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "device_os")
    private String deviceOs;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    @Column(name = "geo_city")
    private String geoCity;

    @Column(name = "geo_country")
    private String geoCountry;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "signup_time")
    private LocalDateTime signupTime;

    @Column(name = "verified")
    private boolean verified;
}
