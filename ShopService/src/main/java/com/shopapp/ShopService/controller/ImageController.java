package com.shopapp.ShopService.controller;

import com.shopapp.ShopService.dto.image.response.ImageResponseDTO;
import com.shopapp.ShopService.exception.ResourceNotFoundException;
import com.shopapp.ShopService.model.Image;
import com.shopapp.ShopService.service.ImageService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageResponseDTO> uploadImage(
            @RequestParam UUID shopId,
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading image for Shop ID: {}", shopId);
        ImageResponseDTO image = imageService.uploadImage(shopId, file);
        return ResponseEntity.ok(image);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        log.info("Deleting Image ID: {}", imageId);
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shops/{shopId}")
    public ResponseEntity<List<ImageResponseDTO>> getImagesByShop(@PathVariable UUID shopId) {
        log.info("Fetching images for Shop ID: {}", shopId);
        List<ImageResponseDTO> images = imageService.getImagesByShop(shopId);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{imageId}/download")
    public ResponseEntity<byte[]> downloadImage(@PathVariable UUID imageId) {
        log.info("Downloading Image ID: {}", imageId);

        Image image = imageService.getImageById(imageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                .body(image.getImageData());
    }
}

