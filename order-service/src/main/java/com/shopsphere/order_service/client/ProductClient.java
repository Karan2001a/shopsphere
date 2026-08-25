package com.shopsphere.order_service.client;

import com.shopsphere.order_service.dto.ProductClientResponse;
import com.shopsphere.order_service.dto.StockUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "product-service-client",
        url = "${services.product.url}"
)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductClientResponse getProductById(
            @PathVariable("id") Long id
    );

    @PatchMapping(
            value = "/api/products/internal/{id}/reduce-stock",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ProductClientResponse reduceStock(
            @PathVariable("id") Long id,
            @RequestBody StockUpdateRequest request
    );
}