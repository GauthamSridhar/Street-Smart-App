package com.example.shopapp.controller;

import com.example.shopapp.dto.request.ImageUploadDTO;
import com.example.shopapp.dto.response.ShopRatingsSummary;
import com.example.shopapp.service.ImageService;
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
    public ResponseEntity<ShopRatingsSummary.ImageResponseDTO> uploadImage(
            @RequestParam UUID shopId,
            @Valid @RequestBody ImageUploadDTO imageDTO) {
        log.info("Uploading image for Shop ID: {}", shopId);
        ShopRatingsSummary.ImageResponseDTO image = imageService.uploadImage(shopId, imageDTO);
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
    public ResponseEntity<List<ShopRatingsSummary.ImageResponseDTO>> getImagesByShop(@PathVariable UUID shopId) {
        log.info("Fetching images for Shop ID: {}", shopId);
        List<ShopRatingsSummary.ImageResponseDTO> images = imageService.getImagesByShop(shopId);
        return ResponseEntity.ok(images);
    }
}
