package com.example.shopapp.mapper;

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
        user.setRole(request.getRole());
        return user;
    }

    /**
     * Updates an existing User entity with data from UserRequestDTO.
     *
     * @param user    the existing User entity
     * @param request the UserRequestDTO containing updated data
     * @return the updated User entity
     */
    public User updateEntity(User user, UserRequestDTO request) {
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        // Password update can be handled separately if needed
        return user;
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
        response.setRole(user.getRole());
        response.setVerified(user.isVerified());
        return response;
    }
}
