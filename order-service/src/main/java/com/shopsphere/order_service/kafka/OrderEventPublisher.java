package com.shopsphere.order_service.kafka;

import com.shopsphere.order_service.event.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    private final String orderCreatedTopic;

    public OrderEventPublisher(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
            @Value("${kafka.topic.order-created}")
            String orderCreatedTopic) {

        this.kafkaTemplate = kafkaTemplate;
        this.orderCreatedTopic = orderCreatedTopic;
    }

    public void publishOrderCreatedEvent(
            OrderCreatedEvent event) {

        String key =
                String.valueOf(event.getOrderId());

        kafkaTemplate.send(
                orderCreatedTopic,
                key,
                event
        );
    }
}