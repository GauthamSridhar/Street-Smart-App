package com.example.shopapp.controller;

import com.example.shopapp.model.Rating;
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
    public ResponseEntity<Rating> addRating(
            @RequestParam UUID userId,
            @RequestParam UUID shopId,
            @Valid @RequestBody Rating rating) {
        Rating addedRating = ratingService.addRating(userId, shopId, rating);
        return ResponseEntity.ok(addedRating);
    }
    @GetMapping("/{ratingId}")
    public ResponseEntity<Rating> getRating(@PathVariable UUID ratingId) {
        Rating rating = ratingService.getRating(ratingId);
        return ResponseEntity.ok(rating);
    }
    @PutMapping("/{ratingId}")
    public ResponseEntity<Rating> updateRating(
            @RequestParam UUID userId,
            @PathVariable UUID ratingId,
            @Valid @RequestBody Rating rating) {
        Rating updatedRating = ratingService.updateRating(userId, ratingId, rating);
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
    public ResponseEntity<List<Rating>> getShopRatings(@PathVariable UUID shopId) {
        List<Rating> ratings = ratingService.getShopRatings(shopId);
        return ResponseEntity.ok(ratings);
    }
}
