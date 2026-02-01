package com.user.authentication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // -----------------------------
    // sendOtp - success
    // -----------------------------
    @Test
    void sendOtp_shouldSendEmailSuccessfully() throws MessagingException {
        emailService.sendOtp("user@example.com", "123456", "John");
        verify(mailSender, times(1)).send(mimeMessage);
    }

    // -----------------------------
    // sendOtp - MessagingException path
    // -----------------------------
    @Test
    void sendOtp_shouldThrowRuntimeExceptionOnMessagingException() throws MessagingException {
        // Make MimeMessageHelper throw MessagingException when setting text
        MimeMessageHelper helper = mock(MimeMessageHelper.class);
        try (MockedConstruction<MimeMessageHelper> ignored = mockConstruction(MimeMessageHelper.class,
                (mock, context) -> {
                    doThrow(new MessagingException("Messaging error")).when(mock).setText(anyString(), anyBoolean());
                })) {
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> emailService.sendOtp("user@example.com", "123456", "John"));
            assertEquals("Failed to send OTP email", ex.getMessage());
        }
    }

    // -----------------------------
    // sendPasswordResetEmail - success
    // -----------------------------
    @Test
    void sendPasswordResetEmail_shouldSendEmailSuccessfully() {
        emailService.sendPasswordResetEmail("user@example.com", "John", "http://link");
        verify(mailSender, times(1)).send(mimeMessage);
    }

    // -----------------------------
    // sendPasswordResetEmail - MessagingException path
    // -----------------------------
    @Test
    void sendPasswordResetEmail_shouldThrowRuntimeExceptionOnMessagingException() throws MessagingException {
        try (MockedConstruction<MimeMessageHelper> ignored = mockConstruction(MimeMessageHelper.class,
                (mock, context) -> {
                    doThrow(new MessagingException("Messaging error")).when(mock).setText(anyString(), anyBoolean());
                })) {
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> emailService.sendPasswordResetEmail("user@example.com", "John", "http://link"));
            assertEquals("Failed to send password reset email", ex.getMessage());
        }
    }
}
