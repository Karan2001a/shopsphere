package com.shopsphere.userservice.service;

import com.shopsphere.userservice.dto.LoginRequest;
import com.shopsphere.userservice.dto.RegisterRequest;
import com.shopsphere.userservice.dto.UserResponse;
import com.shopsphere.userservice.dto.response.LoginResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getUserById(Long id);
}