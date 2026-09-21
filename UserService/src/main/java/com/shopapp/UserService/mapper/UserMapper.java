package com.shopapp.UserService.mapper;

import com.shopapp.UserService.dto.user.request.*;
import com.shopapp.UserService.dto.user.response.UserResponse;
import com.shopapp.UserService.model.User;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public User toEntity(RegisterUserRequest request) {
    var user = new User();
    user.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
    user.setFullName(request.getFullName().trim());
    user.setPhoneNumber(request.getPhoneNumber());
    user.setRole(request.getRole());
    return user;
  }

  public void updateEntity(User user, UpdateUserRequest request) {
    user.setFullName(request.getFullName().trim());
    user.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
    user.setPhoneNumber(request.getPhoneNumber());
  }

  public UserResponse toResponse(User user) {
    var response = new UserResponse();
    response.setId(user.getId());
    response.setEmail(user.getEmail());
    response.setFullName(user.getFullName());
    response.setPhoneNumber(user.getPhoneNumber());
    response.setVerified(user.isVerified());
    return response;
  }
}
