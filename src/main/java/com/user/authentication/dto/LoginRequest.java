package com.user.authentication.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private String DeviceName;
    private String DeviceOs;
    private String DeviceFingerprint;
    private String GeoCity;
    private String GeoCountry;
}
