package com.shopsphere.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;

    private Long userId;

    private String userEmail;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    private List<OrderItemEvent> items;
}