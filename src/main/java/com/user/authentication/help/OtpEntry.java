package com.user.authentication.help;

import java.time.LocalDateTime;

public class OtpEntry {

    private final String otp;
    private final LocalDateTime timestamp;

    // Constructor
    public OtpEntry(String otp, LocalDateTime timestamp) {
        this.otp = otp;
        this.timestamp = timestamp;
    }

    // Getter for OTP
    public String getOtp() {
        return otp;
    }

    // Getter for timestamp
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Optional: useful for debugging
    @Override
    public String toString() {
        return "OtpEntry{" +
                "otp='" + otp + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
