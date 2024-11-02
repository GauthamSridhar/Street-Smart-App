// dto/response/ShopResponse.java
package com.example.shopapp.dto.response;

import com.example.shopapp.model.ShopStatus;
import lombok.Data;
import java.util.UUID;

@Data
public class ShopResponse {
    private UUID id;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private ShopStatus status;
    private Float averageRating;
    private UserResponse owner;
}
