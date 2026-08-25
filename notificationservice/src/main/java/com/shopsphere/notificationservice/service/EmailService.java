package com.shopsphere.notificationservice.service;

import com.shopsphere.notificationservice.dto.EmailNotification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(EmailNotification notification) {

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(
                    notification.getTo()
            );

            message.setSubject(
                    notification.getSubject()
            );

            message.setText(
                    notification.getBody()
            );

            mailSender.send(message);

            System.out.println(
                    "EMAIL SENT SUCCESSFULLY → "
                            + notification.getTo()
            );

        } catch (Exception exception) {

            System.err.println(
                    "EMAIL SENDING FAILED → "
                            + notification.getTo()
            );

            System.err.println(
                    "ERROR → "
                            + exception.getMessage()
            );

            throw exception;
        }
    }
}