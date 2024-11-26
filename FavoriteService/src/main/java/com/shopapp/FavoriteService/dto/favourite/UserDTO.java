package com.shopapp.FavoriteService.dto.favourite;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private List<UUID> favourites = new ArrayList<>();
    // Include other necessary fields if required
}
