package com.shopapp.UserService.dto.user;

import java.util.UUID;

public record JwtToken (String jwt, String username){
}