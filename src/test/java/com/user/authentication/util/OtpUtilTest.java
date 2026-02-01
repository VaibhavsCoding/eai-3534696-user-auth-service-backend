package com.user.authentication.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpUtilTest {

    @Test
    void generateOtp_shouldGenerateOtpOfGivenLength() {
        int length = 6;

        String otp = OtpUtil.generateOtp(length);

        assertNotNull(otp);
        assertEquals(length, otp.length());
        assertTrue(otp.matches("\\d{" + length + "}"));
    }

    @Test
    void generateOtp_shouldGenerateZeroPaddedOtp() {
        int length = 4;

        // Run multiple times to increase chance of leading zeros
        boolean foundLeadingZero = false;
        for (int i = 0; i < 50; i++) {
            String otp = OtpUtil.generateOtp(length);
            if (otp.startsWith("0")) {
                foundLeadingZero = true;
                break;
            }
        }

        assertTrue(foundLeadingZero, "OTP should support leading zeros");
    }

    @Test
    void generateOtp_shouldGenerateDifferentOtps() {
        String otp1 = OtpUtil.generateOtp(6);
        String otp2 = OtpUtil.generateOtp(6);

        assertNotEquals(otp1, otp2);
    }

    @Test
    void generateOtp_shouldThrowExceptionWhenLengthIsZero() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class,
                        () -> OtpUtil.generateOtp(0));

        assertEquals("OTP length must be greater than 0", exception.getMessage());
    }

    @Test
    void generateOtp_shouldThrowExceptionWhenLengthIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> OtpUtil.generateOtp(-4));
    }
}
