package com.shopsphere.order_service.service.Impl;

import com.shopsphere.order_service.client.ProductClient;
import com.shopsphere.order_service.client.UserClient;
import com.shopsphere.order_service.dto.OrderItemRequest;
import com.shopsphere.order_service.dto.OrderItemResponse;
import com.shopsphere.order_service.dto.OrderRequest;
import com.shopsphere.order_service.dto.OrderResponse;
import com.shopsphere.order_service.dto.ProductClientResponse;
import com.shopsphere.order_service.dto.StockUpdateRequest;
import com.shopsphere.order_service.dto.UserClientResponse;
import com.shopsphere.order_service.entity.Order;
import com.shopsphere.order_service.entity.OrderIteam;
import com.shopsphere.order_service.enums.OrderStatus;
import com.shopsphere.order_service.event.OrderCreatedEvent;
import com.shopsphere.order_service.event.OrderItemEvent;
import com.shopsphere.order_service.exception.InsufficientStockException;
import com.shopsphere.order_service.exception.OrderNotFoundException;
import com.shopsphere.order_service.exception.RemoteServiceException;
import com.shopsphere.order_service.kafka.OrderEventPublisher;
import com.shopsphere.order_service.repository.OrderRepository;
import com.shopsphere.order_service.service.OrderService;

import feign.FeignException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final OrderEventPublisher orderEventPublisher;

    /*
     * Constructor Injection
     *
     * Spring automatically injects:
     *
     * OrderRepository
     * UserClient
     * ProductClient
     * OrderEventPublisher
     */
    public OrderServiceImpl(
            OrderRepository orderRepository,
            UserClient userClient,
            ProductClient productClient,
            OrderEventPublisher orderEventPublisher) {

        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.productClient = productClient;
        this.orderEventPublisher = orderEventPublisher;
    }

    /*
     * ==========================================================
     * CREATE ORDER
     * ==========================================================
     */
    @Override
    public OrderResponse createOrder(OrderRequest request) {

        /*
         * Step 1
         *
         * Validate user through User Service.
         *
         * We keep the returned user because later we need
         * the customer's email for Kafka notification.
         */
        UserClientResponse user =
                validateUser(request.getUserId());

        LocalDateTime currentTime =
                LocalDateTime.now();

        /*
         * Step 2
         *
         * Create the main Order entity.
         *
         * Initially totalAmount = 0 because we calculate
         * it using actual Product Service prices.
         */
        Order order = Order.builder()
                .userId(request.getUserId())
                .totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        /*
         * Step 3
         *
         * Process every product requested by customer.
         */
        for (OrderItemRequest itemRequest :
                request.getItems()) {

            /*
             * Get real product information
             * from Product Service.
             */
            ProductClientResponse product =
                    getProduct(
                            itemRequest.getProductId()
                    );

            /*
             * Validate available stock.
             */
            validateStock(
                    product,
                    itemRequest.getQuantity()
            );

            /*
             * Reduce inventory using Product Service.
             */
            reduceProductStock(
                    product.getId(),
                    itemRequest.getQuantity()
            );

            /*
             * Calculate item subtotal.
             *
             * price × quantity
             *
             * Example:
             *
             * 1299.99 × 3
             * =
             * 3899.97
             */
            BigDecimal subtotal =
                    product.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            itemRequest
                                                    .getQuantity()
                                    )
                            );

            /*
             * Create Order Item.
             *
             * We store a snapshot of:
             *
             * Product ID
             * Product Name
             * Product Price
             *
             * because Product Service data may change later.
             */
            OrderIteam orderItem =
                    OrderIteam.builder()
                            .productId(
                                    product.getId()
                            )
                            .productName(
                                    product.getName()
                            )
                            .price(
                                    product.getPrice()
                            )
                            .quantity(
                                    itemRequest.getQuantity()
                            )
                            .subtotal(
                                    subtotal
                            )
                            .build();

            /*
             * Add item to order.
             *
             * Your Order.addItem() should internally do:
             *
             * items.add(item);
             * item.setOrder(this);
             */
            order.addItem(orderItem);

            /*
             * Add subtotal to complete order total.
             */
            totalAmount =
                    totalAmount.add(subtotal);
        }

        /*
         * IMPORTANT
         *
         * This must happen AFTER the for-loop.
         */
        order.setTotalAmount(totalAmount);

        /*
         * Step 4
         *
         * Save Order and OrderItems.
         *
         * CascadeType.ALL should automatically save
         * every OrderItem.
         */
        Order savedOrder =
                orderRepository.save(order);

        /*
         * Step 5
         *
         * Convert saved order into a Kafka event.
         */
        OrderCreatedEvent event =
                buildOrderCreatedEvent(
                        savedOrder,
                        user.getEmail()
                );

        /*
         * Step 6
         *
         * Publish event to Kafka.
         *
         * Topic:
         *
         * order-created
         */
        orderEventPublisher
                .publishOrderCreatedEvent(event);

        /*
         * Step 7
         *
         * Convert entity into API response.
         */
        return mapToResponse(savedOrder);
    }

    /*
     * ==========================================================
     * REDUCE PRODUCT STOCK
     * ==========================================================
     */
    private void reduceProductStock(
            Long productId,
            Integer quantity) {

        try {

            productClient.reduceStock(
                    productId,
                    new StockUpdateRequest(quantity)
            );

        } catch (FeignException.Conflict exception) {

            throw new InsufficientStockException(
                    "Unable to reduce stock for product ID: "
                            + productId
            );

        } catch (FeignException.NotFound exception) {

            throw new RemoteServiceException(
                    "Product not found with ID: "
                            + productId,
                    exception
            );

        } catch (FeignException exception) {

            throw new RemoteServiceException(
                    "Unable to update stock in Product Service. "
                            + "HTTP Status: "
                            + exception.status()
                            + " | Response: "
                            + exception.contentUTF8(),
                    exception
            );
        }
    }

    /*
     * ==========================================================
     * GET ORDER BY ID
     * ==========================================================
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(
            Long id) {

        Order order =
                findOrderById(id);

        return mapToResponse(order);
    }

    /*
     * ==========================================================
     * GET ALL ORDERS
     * ==========================================================
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * ==========================================================
     * GET ORDERS BY USER ID
     * ==========================================================
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(
            Long userId) {

        /*
         * Validate user first.
         */
        validateUser(userId);

        return orderRepository
                .findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /*
     * ==========================================================
     * CANCEL ORDER
     * ==========================================================
     */
    @Override
    public void cancelOrder(Long id) {

        Order order =
                findOrderById(id);

        /*
         * Shipped and delivered orders cannot
         * be cancelled.
         */
        if (order.getStatus()
                == OrderStatus.SHIPPED
                ||
                order.getStatus()
                        == OrderStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Shipped or delivered orders "
                            + "cannot be cancelled"
            );
        }

        /*
         * Prevent cancelling the same order twice.
         */
        if (order.getStatus()
                == OrderStatus.CANCELLED) {

            throw new IllegalStateException(
                    "Order is already cancelled"
            );
        }

        order.setStatus(
                OrderStatus.CANCELLED
        );

        order.setUpdatedAt(
                LocalDateTime.now()
        );

        orderRepository.save(order);
    }

    /*
     * ==========================================================
     * VALIDATE USER THROUGH FEIGN
     * ==========================================================
     */
    private UserClientResponse validateUser(
            Long userId) {

        try {

            UserClientResponse user =
                    userClient
                            .getUserById(userId);

            if (user == null
                    || user.getId() == null) {

                throw new RemoteServiceException(
                        "User Service returned "
                                + "an invalid response"
                );
            }

            return user;

        } catch (FeignException.NotFound exception) {

            throw new RemoteServiceException(
                    "User not found with ID: "
                            + userId,
                    exception
            );

        } catch (FeignException exception) {

            throw new RemoteServiceException(
                    "Unable to communicate "
                            + "with User Service",
                    exception
            );
        }
    }

    /*
     * ==========================================================
     * GET PRODUCT THROUGH FEIGN
     * ==========================================================
     */
    private ProductClientResponse getProduct(
            Long productId) {

        try {

            ProductClientResponse product =
                    productClient
                            .getProductById(productId);

            if (product == null
                    || product.getId() == null) {

                throw new RemoteServiceException(
                        "Product Service returned "
                                + "an invalid response"
                );
            }

            return product;

        } catch (FeignException.NotFound exception) {

            throw new RemoteServiceException(
                    "Product not found with ID: "
                            + productId,
                    exception
            );

        } catch (FeignException exception) {

            throw new RemoteServiceException(
                    "Unable to communicate "
                            + "with Product Service",
                    exception
            );
        }
    }

    /*
     * ==========================================================
     * VALIDATE PRODUCT STOCK
     * ==========================================================
     */
    private void validateStock(
            ProductClientResponse product,
            Integer requestedQuantity) {

        /*
         * Product Service should always send
         * stockQuantity.
         */
        if (product.getStockQuantity() == null) {

            throw new RemoteServiceException(
                    "Product stock information "
                            + "is unavailable for product ID: "
                            + product.getId()
            );
        }

        /*
         * Example:
         *
         * Available: 5
         * Requested: 10
         *
         * → Reject order.
         */
        if (product.getStockQuantity()
                < requestedQuantity) {

            throw new InsufficientStockException(
                    "Insufficient stock for product "
                            + product.getName()
                            + ". Available: "
                            + product.getStockQuantity()
                            + ", requested: "
                            + requestedQuantity
            );
        }
    }

    /*
     * ==========================================================
     * FIND ORDER
     * ==========================================================
     */
    private Order findOrderById(Long id) {

        return orderRepository
                .findById(id)
                .orElseThrow(() ->

                        new OrderNotFoundException(
                                "Order not found with ID: "
                                        + id
                        )
                );
    }

    /*
     * ==========================================================
     * ENTITY → API RESPONSE
     * ==========================================================
     */
    private OrderResponse mapToResponse(
            Order order) {

        List<OrderItemResponse> itemResponses =
                order.getItems()
                        .stream()
                        .map(
                                this::mapItemToResponse
                        )
                        .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalAmount(
                        order.getTotalAmount()
                )
                .status(
                        order.getStatus()
                )
                .createdAt(
                        order.getCreatedAt()
                )
                .updatedAt(
                        order.getUpdatedAt()
                )
                .items(itemResponses)
                .build();
    }

    /*
     * ==========================================================
     * ORDER ITEM → API RESPONSE
     * ==========================================================
     */
    private OrderItemResponse mapItemToResponse(
            OrderIteam item) {

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(
                        item.getProductId()
                )
                .productName(
                        item.getProductName()
                )
                .price(
                        item.getPrice()
                )
                .quantity(
                        item.getQuantity()
                )
                .subtotal(
                        item.getSubtotal()
                )
                .build();
    }

    /*
     * ==========================================================
     * ORDER → KAFKA EVENT
     * ==========================================================
     */
    private OrderCreatedEvent buildOrderCreatedEvent(
            Order order,
            String userEmail) {

        /*
         * Convert every OrderItem into
         * an OrderItemEvent.
         */
        List<OrderItemEvent> eventItems =
                order.getItems()
                        .stream()
                        .map(item ->

                                OrderItemEvent.builder()
                                        .productId(
                                                item.getProductId()
                                        )
                                        .productName(
                                                item.getProductName()
                                        )
                                        .price(
                                                item.getPrice()
                                        )
                                        .quantity(
                                                item.getQuantity()
                                        )
                                        .subtotal(
                                                item.getSubtotal()
                                        )
                                        .build()
                        )
                        .toList();

        /*
         * Create the final Kafka event.
         */
        return OrderCreatedEvent.builder()
                .orderId(
                        order.getId()
                )
                .userId(
                        order.getUserId()
                )
                .userEmail(
                        userEmail
                )
                .totalAmount(
                        order.getTotalAmount()
                )
                .createdAt(
                        order.getCreatedAt()
                )
                .items(
                        eventItems
                )
                .build();
    }
}