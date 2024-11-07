package com.example.shopapp.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class UserResponseDTO {

    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;  // This can be replaced with an enum
    private boolean isVerified;
}
