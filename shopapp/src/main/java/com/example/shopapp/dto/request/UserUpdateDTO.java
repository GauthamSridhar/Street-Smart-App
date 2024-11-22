package com.example.shopapp.dto.request;

import lombok.Data;

@Data
public class UserUpdateDTO {

    private String fullName;

    private String email;

    private String phoneNumber;

//    private String role;  // This could be restricted for admins only if required
}
