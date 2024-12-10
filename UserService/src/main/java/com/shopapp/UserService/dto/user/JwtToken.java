package com.shopapp.UserService.dto.user;

import com.shopapp.UserService.model.UserRole;

import java.util.UUID;

public record JwtToken (String jwt, String username, String role,String id){
}