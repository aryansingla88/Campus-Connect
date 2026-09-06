package com.campus.Campus_Connect.features.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetOtp(
            String recipientEmail,
            String otp
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(recipientEmail);

        message.setSubject(
                "Campus Connect - Password Reset OTP"
        );

        message.setText(
                """
                Hello,

                Your Campus Connect password reset OTP is:

                %s

                This OTP expires in 10 minutes.

                If you did not request a password reset,
                you can safely ignore this email.

                Campus Connect
                """.formatted(otp)
        );

        mailSender.send(message);
    }
}