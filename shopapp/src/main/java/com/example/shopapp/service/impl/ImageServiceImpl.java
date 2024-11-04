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
    private final ShopRepository shopRepository; // Inject ShopRepository to validate shop existence

    @Override
    public Image uploadImage(UUID shopId, String imageUrl) {
        // Check if the shop exists before creating an image
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));

        Image image = new Image();
        image.setShop(shop); // Set the existing shop to the image
        image.setImageUrl(imageUrl);
        return imageRepository.save(image);
    }

    @Override
    public void deleteImage(UUID imageId) {
        // Check if the image exists before deletion
        if (!imageRepository.existsById(imageId)) {
            throw new ResourceNotFoundException("Image with ID " + imageId + " not found");
        }
        imageRepository.deleteById(imageId);
    }

    @Override
    public List<Image> getImagesByShop(UUID shopId) {
        // You could add error handling here too if desired, but this assumes shops exist
        return imageRepository.findByShopId(shopId);
    }
}
