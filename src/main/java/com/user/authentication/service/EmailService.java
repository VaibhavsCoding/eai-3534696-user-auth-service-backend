package com.user.authentication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String APP_NAME = "React App";

    /**
     * Sends OTP email for user verification with HTML formatting.
     * @param toEmail recipient email
     * @param otp generated OTP
     * @param userName recipient name (optional)
     */
    @Async
    public void sendOtp(String toEmail, String otp, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(APP_NAME + " - Your OTP Code");

            String html = "<div style='font-family:Arial,sans-serif;font-size:15px;color:#333;'>"
                    + "<p>Hi,</p>"
                    + "<p>Your one-time password (OTP) for verifying your account is:</p>"
                    + "<h2 style='color:#2e6c80;'>" + otp + "</h2>"
                    + "<p>This OTP will expire in <b>2 minutes</b>.</p>"
                    + "<p>⚠️ Please do not share this OTP with anyone.</p>"
                    + "<p>Thanks,<br>The " + APP_NAME + " Team</p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    /**
     * Sends password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String name, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password — " + APP_NAME);

            String html = """
                    <div style="font-family:Arial,sans-serif; color:#333; font-size:15px; line-height:1.6;">
                        <p>Hi %s,</p>
                        <p>We received a request to reset the password for your <b>%s</b> account associated with <b>%s</b>.</p>
                        <p>If you made this request, please click the button below to set a new password:</p>
                        <p style="text-align:center; margin:25px 0;">
                            <a href="%s" style="background-color:#007bff; color:#fff; padding:12px 25px;
                               text-decoration:none; border-radius:5px; font-weight:bold;">
                               👉 Reset My Password
                            </a>
                        </p>
                        <p>This link will expire in <b>15 minutes</b> for your security.</p>
                        <p>If you didn’t request a password reset, please ignore this email — your account will remain safe and unchanged.</p>
                        <hr style="border:none;border-top:1px solid #ddd;margin:20px 0;">
                        <p><b>⚠️ Security Notice:</b><br>
                        For your protection, do not share this link or your password with anyone.<br>
                        %s will never ask for your credentials by email or phone.</p>
                        <p>Thank you for being part of the %s community!<br>
                        We’re here to help — if you need assistance, just reply to this email or visit our Help Center.</p>
                        <p>Warm regards,<br>The %s Team</p>
                    </div>
                    """.formatted(name, APP_NAME, toEmail, resetLink, APP_NAME, APP_NAME, APP_NAME);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
