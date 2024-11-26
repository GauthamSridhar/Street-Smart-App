package com.shopapp.ShopService.controller;


import com.shopapp.ShopService.dto.image.request.ImageUploadDTO;
import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;
import com.shopapp.ShopService.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for image-related operations.
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageService imageService;

    /**
     * Uploads an image for a shop.
     *
     * @param shopId   the ID of the shop
     * @param imageDTO the image upload request DTO
     * @return the uploaded image response DTO
     */
    @PostMapping
    public ResponseEntity<ImageResponseDTO> uploadImage(
            @RequestParam UUID shopId,
            @Valid @RequestBody ImageUploadDTO imageDTO) {
        log.info("Uploading image for Shop ID: {}", shopId);
        ImageResponseDTO image = imageService.uploadImage(shopId, imageDTO);
        return ResponseEntity.ok(image);
    }

    /**
     * Deletes an image.
     *
     * @param imageId the ID of the image to delete
     * @return a 204 No Content response
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        log.info("Deleting Image ID: {}", imageId);
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Fetches all images for a specific shop.
     *
     * @param shopId the ID of the shop
     * @return a list of image response DTOs
     */
    @GetMapping("/shops/{shopId}")
    public ResponseEntity<List<ImageResponseDTO>> getImagesByShop(@PathVariable UUID shopId) {
        log.info("Fetching images for Shop ID: {}", shopId);
        List<ImageResponseDTO> images = imageService.getImagesByShop(shopId);
        return ResponseEntity.ok(images);
    }
}
