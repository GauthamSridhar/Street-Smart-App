// Define ShopBasicInfoDTO inside FavoriteService
package com.shopapp.FavoriteService.dto.favourite;

import lombok.Data;
import java.util.UUID;

@Data
public class ShopBasicInfoDTO {
    private UUID id;
    private String name;
}
