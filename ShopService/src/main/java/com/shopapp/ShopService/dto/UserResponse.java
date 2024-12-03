package com.shopapp.ShopService.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private List<UUID> ratings;
    private List<UUID> favorites;
}
