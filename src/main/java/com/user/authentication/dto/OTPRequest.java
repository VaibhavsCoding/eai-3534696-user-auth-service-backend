package com.user.authentication.dto;

import lombok.Data;

@Data
public class OTPRequest {
    private String email;
    private String otp;
}
