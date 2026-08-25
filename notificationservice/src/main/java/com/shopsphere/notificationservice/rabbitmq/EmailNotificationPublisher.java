package com.shopsphere.notificationservice.rabbitmq;

import com.shopsphere.notificationservice.dto.EmailNotification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final String exchange;

    private final String routingKey;

    public EmailNotificationPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${rabbitmq.exchange.notification}")
            String exchange,
            @Value("${rabbitmq.routing-key.email}")
            String routingKey) {

        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(
            EmailNotification notification) {

        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                notification
        );
    }
}