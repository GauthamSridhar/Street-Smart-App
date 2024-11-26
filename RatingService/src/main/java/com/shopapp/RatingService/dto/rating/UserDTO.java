package com.shopapp.RatingService.dto.rating;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private List<UUID> ratings = new ArrayList<>();
}
