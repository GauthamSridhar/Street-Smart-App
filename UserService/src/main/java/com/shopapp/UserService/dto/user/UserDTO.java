package com.shopapp.UserService.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserDTO {
    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private List<UUID> ratings;
    private List<UUID> favorites;

}
