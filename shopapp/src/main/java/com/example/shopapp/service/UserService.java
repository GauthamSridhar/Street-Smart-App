// service/UserService.java
package com.example.shopapp.service;

import com.example.shopapp.dto.request.LoginRequest;
import com.example.shopapp.model.User;
import java.util.UUID;

public interface UserService {
    User register(User request);
    User login(LoginRequest request);
    User updateProfile(UUID userId, User request);

    User findUserById(UUID userId);
}
