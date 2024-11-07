package com.example.shopapp.controller;

import com.example.shopapp.dto.ImageResponseDTO;
import com.example.shopapp.dto.ImageUploadDTO;
import com.example.shopapp.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<ImageResponseDTO> uploadImage(
            @RequestParam UUID shopId,
            @Valid @RequestBody ImageUploadDTO imageDTO) {
        ImageResponseDTO image = imageService.uploadImage(shopId, imageDTO);
        return ResponseEntity.ok(image);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shops/{shopId}")
    public ResponseEntity<List<ImageResponseDTO>> getImagesByShop(@PathVariable UUID shopId) {
        List<ImageResponseDTO> images = imageService.getImagesByShop(shopId);
        return ResponseEntity.ok(images);
    }
}