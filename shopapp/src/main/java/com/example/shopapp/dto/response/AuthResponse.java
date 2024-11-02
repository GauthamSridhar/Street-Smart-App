// dto/response/AuthResponse.java
package com.example.shopapp.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private UserResponse user;
}
