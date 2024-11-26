package com.shopapp.UserService.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email or phone number is mandatory")
    private String identifier;

    @NotBlank(message = "Password is mandatory")
    private String password;
}
