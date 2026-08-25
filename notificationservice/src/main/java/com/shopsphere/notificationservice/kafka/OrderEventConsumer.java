package com.shopsphere.notificationservice.kafka;

import com.shopsphere.notificationservice.dto.EmailNotification;
import com.shopsphere.notificationservice.event.OrderCreatedEvent;
import com.shopsphere.notificationservice.event.OrderItemEvent;
import com.shopsphere.notificationservice.rabbitmq.EmailNotificationPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    private final EmailNotificationPublisher publisher;

    public OrderEventConsumer(
            EmailNotificationPublisher publisher) {

        this.publisher = publisher;
    }

    @KafkaListener(
            topics = "${kafka.topic.order-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(OrderCreatedEvent event) {

        System.out.println(
                "KAFKA EVENT RECEIVED → Order ID: "
                        + event.getOrderId()
        );

        String emailBody =
                buildEmailBody(event);

        EmailNotification notification =
                EmailNotification.builder()
                        .to(event.getUserEmail())
                        .subject(
                                "ShopSphere Order Confirmation - Order #"
                                        + event.getOrderId()
                        )
                        .body(emailBody)
                        .build();

        publisher.publish(notification);

        System.out.println(
                "MESSAGE SENT TO RABBITMQ → "
                        + event.getUserEmail()
        );
    }

    private String buildEmailBody(
            OrderCreatedEvent event) {

        StringBuilder body = new StringBuilder();

        body.append("Thank you for your ShopSphere order.")
                .append("\n\n");

        body.append("Order ID: ")
                .append(event.getOrderId())
                .append("\n");

        body.append("Order Date: ")
                .append(event.getCreatedAt())
                .append("\n\n");

        body.append("Items:\n");

        for (OrderItemEvent item : event.getItems()) {

            body.append("- ")
                    .append(item.getProductName())
                    .append(" x ")
                    .append(item.getQuantity())
                    .append(" = $")
                    .append(item.getSubtotal())
                    .append("\n");
        }

        body.append("\nTotal: $")
                .append(event.getTotalAmount())
                .append("\n\n");

        body.append(
                "Thank you for shopping with ShopSphere."
        );

        return body.toString();
    }
}