package com.example.shopapp.service;

import com.example.shopapp.dto.request.LoginRequest;
import com.example.shopapp.dto.request.UpdateUserRequest;
import com.example.shopapp.dto.request.UserRequestDTO;
import com.example.shopapp.dto.response.UserResponse;
import com.example.shopapp.model.User;

import java.util.UUID;

public interface UserService {
    UserResponse register(UserRequestDTO request);
    UserResponse login(LoginRequest request);
    UserResponse updateProfile(UUID userId, UpdateUserRequest request);
    User findUserById(UUID userId);
}
