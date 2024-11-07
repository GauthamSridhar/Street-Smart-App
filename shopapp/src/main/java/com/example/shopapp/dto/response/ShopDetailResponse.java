

package com.example.shopapp.dto.response;


import lombok.Data;

import java.util.List;

@Data
public class ShopDetailResponse extends ShopResponse {
    private List<ProductResponseDTO> products;
    private List<RatingResponseDTO> ratings;
}