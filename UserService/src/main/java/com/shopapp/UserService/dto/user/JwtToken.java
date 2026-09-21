package com.shopapp.UserService.dto.user;

public record JwtToken(String jwt, String username, String role, String id) {}
