package com.example.shopapp.service;

import com.example.shopapp.model.Image;
import java.util.List;
import java.util.UUID;

public interface ImageService {
    Image uploadImage(UUID shopId, String imageUrl);
    void deleteImage(UUID imageId);
    List<Image> getImagesByShop(UUID shopId);
}
