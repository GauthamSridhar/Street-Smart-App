package com.example.shopapp.service.impl;

import com.example.shopapp.dto.request.ImageUploadDTO;
import com.example.shopapp.dto.response.ShopRatingsSummary;
import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.mapper.ImageMapper;
import com.example.shopapp.model.Image;
import com.example.shopapp.model.Shop;
import com.example.shopapp.repository.ImageRepository;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ShopRepository shopRepository;
    private final ImageMapper imageMapper;

    @Override
    public ShopRatingsSummary.ImageResponseDTO uploadImage(UUID shopId, ImageUploadDTO imageDTO) {
        log.info("Uploading image for Shop ID: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found with ID: " + shopId));

        Image image = imageMapper.toEntity(imageDTO);
        image.setShop(shop);
        Image savedImage = imageRepository.save(image);

        log.info("Image uploaded successfully for Shop ID: {}", shopId);
        return imageMapper.toDTO(savedImage);
    }

    @Override
    public void deleteImage(UUID imageId) {
        log.info("Deleting Image ID: {}", imageId);

        if (!imageRepository.existsById(imageId)) {
            throw new ResourceNotFoundException("Image not found with ID: " + imageId);
        }

        imageRepository.deleteById(imageId);
        log.info("Image ID: {} deleted successfully", imageId);
    }

    @Override
    public List<ShopRatingsSummary.ImageResponseDTO> getImagesByShop(UUID shopId) {
        log.info("Fetching images for Shop ID: {}", shopId);

        return imageRepository.findByShopId(shopId).stream()
                .map(imageMapper::toDTO)
                .collect(Collectors.toList());
    }
}
