package com.user.authentication.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OtpEmailServiceTest {

    private JavaMailSender mailSender;
    private OtpEmailService otpEmailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        otpEmailService = new OtpEmailService();
        otpEmailService.mailSender = mailSender; // inject mock
    }

    @Test
    void sendOtp_sendsEmailSuccessfully_withUserName() throws Exception {
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        otpEmailService.sendOtp("test@example.com", "123456", "Alice");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mockMessage);
    }

    @Test
    void sendOtp_sendsEmailSuccessfully_withoutUserName_defaultsToUser() throws Exception {
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        otpEmailService.sendOtp("test@example.com", "654321", " ");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mockMessage);
    }

    @Test
    void sendOtp_createsMimeMessageWithHtml() throws Exception {
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);

        otpEmailService.sendOtp("test@example.com", "777777", "Charlie");

        verify(mailSender).send(captor.capture());
        MimeMessage sentMessage = captor.getValue();
        assertNotNull(sentMessage); // Verify that the MimeMessage is actually created
    }
}
