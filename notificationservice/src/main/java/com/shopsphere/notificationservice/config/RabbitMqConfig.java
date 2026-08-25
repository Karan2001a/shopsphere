package com.shopsphere.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.queue.email}")
    private String emailQueue;

    @Value("${rabbitmq.exchange.notification}")
    private String notificationExchange;

    @Value("${rabbitmq.routing-key.email}")
    private String emailRoutingKey;

    @Bean
    public Queue emailQueue() {

        return new Queue(
                emailQueue,
                true
        );
    }

    @Bean
    public DirectExchange notificationExchange() {

        return new DirectExchange(
                notificationExchange
        );
    }

    @Bean
    public Binding emailBinding(
            Queue emailQueue,
            DirectExchange notificationExchange) {

        return BindingBuilder
                .bind(emailQueue)
                .to(notificationExchange)
                .with(emailRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {

        return new JacksonJsonMessageConverter();
    }
}