package com.shopsphere.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator shopSphereRoutes(
            RouteLocatorBuilder builder,
            @Value("${services.user.url}")
            String userServiceUrl,
            @Value("${services.product.url}")
            String productServiceUrl,
            @Value("${services.order.url}")
            String orderServiceUrl) {

        return builder.routes()

                /*
                 * User Service
                 */
                .route(
                        "user-service",
                        route ->
                                route
                                        .path(
                                                "/api/users/**"
                                        )
                                        .uri(
                                                userServiceUrl
                                        )
                )

                /*
                 * Product Service
                 */
                .route(
                        "product-service",
                        route ->
                                route
                                        .path(
                                                "/api/products/**"
                                        )
                                        .uri(
                                                productServiceUrl
                                        )
                )

                /*
                 * Order Service
                 */
                .route(
                        "order-service",
                        route ->
                                route
                                        .path(
                                                "/api/orders/**"
                                        )
                                        .uri(
                                                orderServiceUrl
                                        )
                )

                .build();
    }
}