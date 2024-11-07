package com.example.shopapp.controller;

import com.example.shopapp.dto.RatingCreateDTO;
import com.example.shopapp.dto.RatingUpdateDTO;
import com.example.shopapp.dto.response.RatingResponseDTO;
import com.example.shopapp.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingResponseDTO> addRating(
            @RequestParam UUID userId,
            @RequestParam UUID shopId,
            @Valid @RequestBody RatingCreateDTO ratingDTO) {
        RatingResponseDTO addedRating = ratingService.addRating(userId, shopId, ratingDTO);
        return ResponseEntity.ok(addedRating);
    }

    @GetMapping("/{ratingId}")
    public ResponseEntity<RatingResponseDTO> getRating(@PathVariable UUID ratingId) {
        RatingResponseDTO rating = ratingService.getRating(ratingId);
        return ResponseEntity.ok(rating);
    }

    @PutMapping("/{ratingId}")
    public ResponseEntity<RatingResponseDTO> updateRating(
            @RequestParam UUID userId,
            @PathVariable UUID ratingId,
            @Valid @RequestBody RatingUpdateDTO ratingDTO) {
        RatingResponseDTO updatedRating = ratingService.updateRating(userId, ratingId, ratingDTO);
        return ResponseEntity.ok(updatedRating);
    }

    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> deleteRating(
            @RequestParam UUID userId,
            @PathVariable UUID ratingId) {
        ratingService.deleteRating(userId, ratingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/shops/{shopId}")
    public ResponseEntity<List<RatingResponseDTO>> getShopRatings(@PathVariable UUID shopId) {
        List<RatingResponseDTO> ratings = ratingService.getShopRatings(shopId);
        return ResponseEntity.ok(ratings);
    }
}
