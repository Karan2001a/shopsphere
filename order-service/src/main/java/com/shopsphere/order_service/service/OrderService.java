package com.shopsphere.order_service.service;


import com.shopsphere.order_service.dto.OrderRequest;
import com.shopsphere.order_service.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByUserId(Long userId);

    void cancelOrder(Long id);
}