package com.user.authentication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OtpEmailService {

    @Autowired
    JavaMailSender mailSender;

    /**
     * Sends OTP email for user verification with HTML formatting.
     * @param toEmail recipient email
     * @param otp generated OTP
     * @param userName recipient name (optional)
     */
    @Async
    public void sendOtp(String toEmail, String otp, String userName) {
        String subject = "Your One-Time Password (OTP) - React App";

        // 📨 HTML body with bold OTP
        String body = String.format("""
                <p>Hi %s,</p>
                <p>Your one-time password for completing access request is:</p>
                <h2 style="font-size: 22px; font-weight: bold; color: #2b6cb0;">%s</h2>
                <p>This one-time password expires after <b>2 minutes</b>.</p>
                <p>If you have not requested this passcode, please report it immediately.</p>
                <p>Thanks,<br>React App</p>
                """,
                (userName != null && !userName.isBlank()) ? userName : "User",
                otp
        );

        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Sends HTML email using MimeMessage
     */
    void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true => HTML enabled

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
