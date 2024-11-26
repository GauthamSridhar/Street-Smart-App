package com.shopapp.UserService.dto.user.request;

import lombok.Data;

@Data
public class UserUpdateDTO {

    private String fullName;

    private String email;

    private String phoneNumber;

}
