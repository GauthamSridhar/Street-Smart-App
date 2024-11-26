package com.shopapp.ShopService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopBasicInfoDTO {
    private UUID id;
    private String name;
}
