package com.shopapp.UserService.dto.user.request;

import com.shopapp.UserService.model.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterUserRequest {
  @NotBlank
  @Email
  @Size(max = 254)
  private String email;

  @NotBlank
  @Size(min = 8, max = 72)
  private String password;

  @NotBlank
  @Size(max = 100)
  private String fullName;

  @NotBlank
  @Pattern(
      regexp = "^\\+[1-9]\\d{9,14}$",
      message = "Use an international phone number, for example +919876543210")
  private String phoneNumber;

  private UserRole role;
  private String phoneVerificationToken;
}
