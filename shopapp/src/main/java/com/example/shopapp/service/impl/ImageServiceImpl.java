package com.example.shopapp.service.impl;

import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.model.Image;
import com.example.shopapp.model.Shop;
import com.example.shopapp.repository.ImageRepository;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;
    private final ShopRepository shopRepository;

    @Override
    public Image uploadImage(UUID shopId, String imageUrl) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));

        Image image = new Image();
        image.setShop(shop);
        image.setImageUrl(imageUrl);
        return imageRepository.save(image);
    }

    @Override
    public void deleteImage(UUID imageId) {
        if (!imageRepository.existsById(imageId)) {
            throw new ResourceNotFoundException("Image with ID " + imageId + " not found");
        }
        imageRepository.deleteById(imageId);
    }

    @Override
    public List<Image> getImagesByShop(UUID shopId) {
        return imageRepository.findByShopId(shopId);
    }
}
