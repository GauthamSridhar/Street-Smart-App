package com.shopapp.UserService.dto.user.response;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phoneNumber;
}
