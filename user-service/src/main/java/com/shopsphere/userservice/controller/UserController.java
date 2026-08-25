package com.shopsphere.userservice.controller;

import com.shopsphere.userservice.dto.LoginRequest;
import com.shopsphere.userservice.dto.RegisterRequest;
import com.shopsphere.userservice.dto.UserResponse;
import com.shopsphere.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.shopsphere.userservice.dto.response.LoginResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse response = userService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);

    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = userService.login(request);

        return ResponseEntity.ok(response);



    }
    @GetMapping("/internal/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        UserResponse response =
                userService.getUserById(id);

        return ResponseEntity.ok(response);
    }
}
