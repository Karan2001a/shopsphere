package com.shopsphere.order_service.client;

import com.shopsphere.order_service.dto.UserClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service-client",
        url = "${services.user.url}"
)
public interface UserClient {

    @GetMapping("/api/users/internal/{id}")
    UserClientResponse getUserById(
            @PathVariable("id") Long id
    );
}