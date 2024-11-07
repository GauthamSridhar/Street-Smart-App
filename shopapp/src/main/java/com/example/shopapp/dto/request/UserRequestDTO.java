package com.example.shopapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequestDTO {

    @NotBlank
    private String fullName;

    @NotBlank
    private String email;

    @NotNull
    private String password;  // In the service layer, this should be hashed

    private String phoneNumber;

    @NotNull
    private String role;  // Should be an enum if possible, for better validation
}
