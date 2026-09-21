package com.shopapp.UserService.dto.user.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateUserRequest {
  @NotBlank
  @Size(max = 100)
  private String fullName;

  @NotBlank
  @Email
  @Size(max = 254)
  private String email;

  @NotBlank
  @Pattern(regexp = "^\\+[1-9]\\d{9,14}$")
  private String phoneNumber;

  @Size(min = 8, max = 72)
  private String password;

  private String currentPassword;
  private String phoneVerificationToken;
}
