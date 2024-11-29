package com.shopapp.UserService.mapper;

import com.shopapp.UserService.dto.user.request.RegisterUserRequest;
import com.shopapp.UserService.dto.user.request.UpdateUserRequest;
import com.shopapp.UserService.dto.user.response.UserResponse;
import com.shopapp.UserService.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterUserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Hashing is expected in the service layer
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());
        return user;
    }

    public void updateEntity(User user, UpdateUserRequest request) {
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setRatings(request.getRatings());
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRatings(user.getRatings());
        return response;
    }
}
