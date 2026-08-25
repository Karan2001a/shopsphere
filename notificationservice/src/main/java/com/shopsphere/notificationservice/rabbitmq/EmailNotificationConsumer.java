package com.shopsphere.notificationservice.rabbitmq;

import com.shopsphere.notificationservice.dto.EmailNotification;
import com.shopsphere.notificationservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationConsumer {

    private final EmailService emailService;

    public EmailNotificationConsumer(
            EmailService emailService) {

        this.emailService = emailService;
    }

    @RabbitListener(
            queues = "${rabbitmq.queue.email}"
    )
    public void consume(
            EmailNotification notification) {

        System.out.println(
                "RABBITMQ EMAIL MESSAGE RECEIVED → "
                        + notification.getTo()
        );

        emailService.sendEmail(notification);
    }
}