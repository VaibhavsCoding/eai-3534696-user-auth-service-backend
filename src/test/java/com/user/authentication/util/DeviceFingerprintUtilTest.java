package com.user.authentication.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeviceFingerprintUtilTest {

    /* ---------- generateFingerprint tests ---------- */

    @Test
    void generateFingerprint_shouldReturnValidHash() {
        String userAgent = "Mozilla/5.0 Chrome";
        String fingerprint = DeviceFingerprintUtil.generateFingerprint(userAgent);

        assertNotNull(fingerprint);
        assertEquals(64, fingerprint.length()); // SHA-256 hex length
        assertTrue(fingerprint.matches("[0-9a-f]+"));
    }

    @Test
    void generateFingerprint_shouldReturnSameHashForSameInput() {
        String userAgent = "Mozilla/5.0 Firefox";

        String hash1 = DeviceFingerprintUtil.generateFingerprint(userAgent);
        String hash2 = DeviceFingerprintUtil.generateFingerprint(userAgent);

        assertEquals(hash1, hash2);
    }

    @Test
    void generateFingerprint_shouldReturnNullWhenUserAgentIsNull() {
        String fingerprint = DeviceFingerprintUtil.generateFingerprint(null);
        assertNull(fingerprint);
    }

    /* ---------- getDeviceType tests ---------- */

    @Test
    void getDeviceType_shouldReturnUnknownDeviceForNullUserAgent() {
        String result = DeviceFingerprintUtil.getDeviceType(null);
        assertEquals("Unknown Device", result);
    }

    @Test
    void getDeviceType_shouldDetectDesktopWindowsChrome() {
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0";
        String result = DeviceFingerprintUtil.getDeviceType(ua);

        assertEquals("Desktop - Windows - Chrome", result);
    }

    @Test
    void getDeviceType_shouldDetectMobileAndroidChrome() {
        String ua = "Mozilla/5.0 (Linux; Android 10; Mobile) Chrome/110.0";
        String result = DeviceFingerprintUtil.getDeviceType(ua);

        assertEquals("Mobile - Android - Chrome", result);
    }

    @Test
    void getDeviceType_shouldDetectTabletIPadSafari() {
        String ua = "Mozilla/5.0 (iPad; CPU OS 14_0 like Mac OS X) Mobile Safari";
        String result = DeviceFingerprintUtil.getDeviceType(ua);

        assertEquals("Tablet - macOS - Safari", result);
    }

    @Test
    void getDeviceType_shouldDetectLinuxFirefox() {
        String ua = "Mozilla/5.0 (X11; Linux x86_64) Firefox/118.0";
        String result = DeviceFingerprintUtil.getDeviceType(ua);

        assertEquals("Desktop - Linux - Firefox", result);
    }

    @Test
    void getDeviceType_shouldDetectEdgeBrowser() {
        String ua = "Mozilla/5.0 (Windows NT 10.0) Edg/120.0";
        String result = DeviceFingerprintUtil.getDeviceType(ua);

        assertEquals("Desktop - Windows - Edge", result);
    }

    @Test
    void getDeviceType_shouldHandleUnknownOsAndBrowser() {
        String ua = "CustomAgent/1.0";
        String result = DeviceFingerprintUtil.getDeviceType(ua);

        assertEquals("Desktop - Unknown OS - Unknown Browser", result);
    }
}
