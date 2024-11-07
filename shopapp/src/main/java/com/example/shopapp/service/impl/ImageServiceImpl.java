
package com.example.shopapp.service.impl;

import com.example.shopapp.dto.ImageResponseDTO;
import com.example.shopapp.dto.ImageUploadDTO;
import com.example.shopapp.exception.ResourceNotFoundException;
import com.example.shopapp.mapper.ImageMapper;
import com.example.shopapp.model.Image;
import com.example.shopapp.model.Shop;
import com.example.shopapp.repository.ImageRepository;
import com.example.shopapp.repository.ShopRepository;
import com.example.shopapp.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;
    private final ShopRepository shopRepository;
    private final ImageMapper imageMapper;

    @Override
    public ImageResponseDTO uploadImage(UUID shopId, ImageUploadDTO imageDTO) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop with ID " + shopId + " not found"));

        Image image = imageMapper.toEntity(imageDTO);
        image.setShop(shop);
        Image savedImage = imageRepository.save(image);

        return imageMapper.toDTO(savedImage);
    }

    @Override
    public void deleteImage(UUID imageId) {
        if (!imageRepository.existsById(imageId)) {
            throw new ResourceNotFoundException("Image with ID " + imageId + " not found");
        }
        imageRepository.deleteById(imageId);
    }

    @Override
    public List<ImageResponseDTO> getImagesByShop(UUID shopId) {
        return imageRepository.findByShopId(shopId).stream()
                .map(imageMapper::toDTO)
                .collect(Collectors.toList());
    }
}
