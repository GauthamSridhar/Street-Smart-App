package com.example.shopapp.mapper;

import com.example.shopapp.dto.request.UpdateUserRequest;
import com.example.shopapp.dto.request.UserRequestDTO;
import com.example.shopapp.dto.response.UserResponse;
import com.example.shopapp.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class to convert between User entity and DTOs.
 */
@Component
public class UserMapper {

    /**
     * Converts UserRequestDTO to User entity.
     *
     * @param request the UserRequestDTO
     * @return the User entity
     */
    public User toEntity(UserRequestDTO request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Note: Password will be hashed in service layer
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        return user;
    }

    /**
     * Updates an existing User entity with data from UpdateUserRequest.
     *
     * @param user    the existing User entity
     * @param request the UpdateUserRequest containing updated data
     */
    public void updateEntity(User user, UpdateUserRequest request) {
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
    }

    /**
     * Converts User entity to UserResponse DTO.
     *
     * @param user the User entity
     * @return the UserResponse DTO
     */
    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        return response;
    }
}
