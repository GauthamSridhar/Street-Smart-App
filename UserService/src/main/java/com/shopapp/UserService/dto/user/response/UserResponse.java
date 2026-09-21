package com.shopapp.UserService.dto.user.response;

import java.util.UUID;
import lombok.Data;

@Data
public class UserResponse {
  private UUID id;
  private String email;
  private String fullName;
  private String phoneNumber;
  private boolean verified;
}
