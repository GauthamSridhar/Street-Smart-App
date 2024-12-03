package com.shopapp.UserService.service;

import com.shopapp.UserService.dto.user.request.LoginRequest;
import com.shopapp.UserService.dto.user.request.RegisterUserRequest;
import com.shopapp.UserService.dto.user.request.UpdateUserRequest;
import com.shopapp.UserService.dto.user.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface UserService {
    UserResponse register(RegisterUserRequest request);

//    UserResponse login(LoginRequest request);

    UserResponse updateProfile(UUID userId, UpdateUserRequest request);

    UserResponse findUserById(UUID userId);

    boolean doesUserExist(UUID userId);

}
