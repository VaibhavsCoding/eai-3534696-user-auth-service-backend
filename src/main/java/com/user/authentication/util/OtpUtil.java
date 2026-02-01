package com.user.authentication.util;

import java.util.Random;

public class OtpUtil {

    private static final Random random = new Random();

    /**
     * Generates a random numeric OTP of given length.
     *
     * @param length number of digits (e.g., 4, 6)
     * @return a zero-padded OTP string
     */
    public static String generateOtp(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be greater than 0");
        }

        int max = (int) Math.pow(10, length);
        int otpNumber = random.nextInt(max);

        return String.format("%0" + length + "d", otpNumber);
    }
}
