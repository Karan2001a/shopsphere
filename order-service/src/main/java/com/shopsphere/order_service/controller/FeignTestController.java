package com.shopsphere.order_service.controller;

import com.shopsphere.order_service.client.ProductClient;
import com.shopsphere.order_service.client.UserClient;
import com.shopsphere.order_service.dto.ProductClientResponse;
import com.shopsphere.order_service.dto.UserClientResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class FeignTestController {

    private final UserClient userClient;
    private final ProductClient productClient;

    public FeignTestController(
            UserClient userClient,
            ProductClient productClient) {

        this.userClient = userClient;
        this.productClient = productClient;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserClientResponse> testUserClient(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userClient.getUserById(id)
        );
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductClientResponse> testProductClient(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productClient.getProductById(id)
        );
    }
}