package com.example.shopapp.controller;

import com.example.shopapp.dto.request.RatingCreateDTO;
import com.example.shopapp.dto.request.RatingUpdateDTO;
import com.example.shopapp.dto.response.RatingResponseDTO;
import com.example.shopapp.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for rating-related operations.
 */
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final RatingService ratingService;

    /**
     * Adds a new rating for a shop.
     *
     * @param userId    the ID of the user adding the rating
     * @param shopId    the ID of the shop being rated
     * @param ratingDTO the rating creation request DTO
     * @return the created rating response DTO
     */
    @PostMapping
    public ResponseEntity<RatingResponseDTO> addRating(
            @RequestParam UUID userId,
            @RequestParam UUID shopId,
            @Valid @RequestBody RatingCreateDTO ratingDTO) {
        log.info("User ID: {} adding rating for Shop ID: {}", userId, shopId);
        RatingResponseDTO addedRating = ratingService.addRating(userId, shopId, ratingDTO);
        return ResponseEntity.ok(addedRating);
    }

    /**
     * Updates an existing rating.
     *
     * @param userId    the ID of the user updating the rating
     * @param ratingId  the ID of the rating to be updated
     * @param ratingDTO the rating update request DTO
     * @return the updated rating response DTO
     */
    @PutMapping("/{ratingId}")
    public ResponseEntity<RatingResponseDTO> updateRating(
            @RequestParam UUID userId,
            @PathVariable UUID ratingId,
            @Valid @RequestBody RatingUpdateDTO ratingDTO) {
        log.info("User ID: {} updating rating ID: {}", userId, ratingId);
        RatingResponseDTO updatedRating = ratingService.updateRating(userId, ratingId, ratingDTO);
        return ResponseEntity.ok(updatedRating);
    }

    /**
     * Deletes a rating.
     *
     * @param userId   the ID of the user deleting the rating
     * @param ratingId the ID of the rating to be deleted
     * @return a 204 No Content response
     */
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> deleteRating(
            @RequestParam UUID userId,
            @PathVariable UUID ratingId) {
        log.info("User ID: {} deleting rating ID: {}", userId, ratingId);
        ratingService.deleteRating(userId, ratingId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Fetches ratings for a specific shop.
     *
     * @param shopId the ID of the shop
     * @return a list of ratings for the shop
     */
    @GetMapping("/shops/{shopId}")
    public ResponseEntity<List<RatingResponseDTO>> getShopRatings(@PathVariable UUID shopId) {
        log.info("Fetching ratings for Shop ID: {}", shopId);
        List<RatingResponseDTO> ratings = ratingService.getShopRatings(shopId);
        return ResponseEntity.ok(ratings);
    }
}
