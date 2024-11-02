// dto/response/UserResponse.java
package com.example.shopapp.dto.response;

import com.example.shopapp.model.UserRole;
import lombok.Data;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private boolean verified;
}
