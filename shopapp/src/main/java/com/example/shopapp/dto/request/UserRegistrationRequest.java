// dto/request/UserRegistrationRequest.java
package com.example.shopapp.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import  jakarta.validation.constraints.Email;
@Data
public class UserRegistrationRequest {
    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String phoneNumber;
}
