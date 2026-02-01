package com.user.authentication.util;

import java.security.MessageDigest;

public class DeviceFingerprintUtil {
    public static String generateFingerprint(String userAgent) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(userAgent.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Determines the device type and OS/browser info from the User-Agent.
     */
    public static String getDeviceType(String userAgent) {
        if (userAgent == null) return "Unknown Device";

        String ua = userAgent.toLowerCase();

        String deviceType;
        if (ua.contains("mobile")) {
            if (ua.contains("tablet") || ua.contains("ipad")) {
                deviceType = "Tablet";
            } else {
                deviceType = "Mobile";
            }
        } else {
            deviceType = "Desktop";
        }

        // Detect OS
        String os;
        if (ua.contains("windows")) os = "Windows";
        else if (ua.contains("mac os")) os = "macOS";
        else if (ua.contains("android")) os = "Android";
        else if (ua.contains("iphone") || ua.contains("ios")) os = "iOS";
        else if (ua.contains("linux")) os = "Linux";
        else os = "Unknown OS";

        // Detect Browser
        String browser;
        if (ua.contains("chrome") && !ua.contains("edg")) browser = "Chrome";
        else if (ua.contains("edg")) browser = "Edge";
        else if (ua.contains("firefox")) browser = "Firefox";
        else if (ua.contains("safari") && !ua.contains("chrome")) browser = "Safari";
        else if (ua.contains("opera") || ua.contains("opr")) browser = "Opera";
        else browser = "Unknown Browser";

        return deviceType + " - " + os + " - " + browser;
    }
}
