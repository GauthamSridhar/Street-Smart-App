package com.example.shopapp.controller;

import com.example.shopapp.model.Image;
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
    public ResponseEntity<Image> uploadImage(
            @RequestParam UUID shopId,
            @RequestParam String imageUrl) {
        Image image = imageService.uploadImage(shopId, imageUrl);
        return ResponseEntity.ok(image);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shops/{shopId}")
    public ResponseEntity<List<Image>> getImagesByShop(@PathVariable UUID shopId) {
        List<Image> images = imageService.getImagesByShop(shopId);
        return ResponseEntity.ok(images);
    }
}
