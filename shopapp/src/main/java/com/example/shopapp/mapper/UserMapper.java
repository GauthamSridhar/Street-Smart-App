



package com.example.shopapp.mapper;

import com.example.shopapp.dto.request.UserRequestDTO;
import com.example.shopapp.dto.response.UserResponse;
import com.example.shopapp.model.User;
import com.example.shopapp.model.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequestDTO request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Note: Password will be hashed in service layer
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.valueOf(String.valueOf(request.getRole())));
        return user;
    }

    public User updateEntity(User user, User request) {
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        return user;
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole());
        response.setVerified(user.isVerified());
        return response;
    }
}