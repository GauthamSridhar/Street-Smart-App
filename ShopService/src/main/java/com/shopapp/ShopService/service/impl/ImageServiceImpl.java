package com.shopapp.ShopService.service.impl;


import com.shopapp.ShopService.dto.image.request.ImageUploadDTO;
import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;
import com.shopapp.ShopService.exception.ResourceNotFoundException;
import com.shopapp.ShopService.mapper.ImageMapper;
import com.shopapp.ShopService.model.Image;
import com.shopapp.ShopService.model.Shop;
import com.shopapp.ShopService.repository.ImageRepository;
import com.shopapp.ShopService.repository.ShopRepository;
import com.shopapp.ShopService.service.ImageService;
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
    public ImageResponseDTO uploadImage(UUID shopId, ImageUploadDTO imageDTO) {
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
    public List<ImageResponseDTO> getImagesByShop(UUID shopId) {
        log.info("Fetching images for Shop ID: {}", shopId);

        return imageRepository.findByShopId(shopId).stream()
                .map(imageMapper::toDTO)
                .collect(Collectors.toList());
    }
}
