package com.example.shopapp.controller;

import com.example.shopapp.dto.request.FavoriteRequestDTO;
import com.example.shopapp.dto.response.FavoriteResponseDTO;
import com.example.shopapp.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for favorite-related operations.
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * Adds a shop to favorites.
     *
     * @param request the favorite request DTO
     * @return the favorite response DTO
     */
    @PostMapping
    public ResponseEntity<FavoriteResponseDTO> addFavorite(
            @Valid @RequestBody FavoriteRequestDTO request) {
        log.info("Adding Shop ID: {} to User ID: {} favorites", request.getShopId(), request.getUserId());
        FavoriteResponseDTO favorite = favoriteService.addFavorite(request.getUserId(), request.getShopId());
        return ResponseEntity.ok(favorite);
    }

    /**
     * Removes a shop from favorites.
     *
     * @param request the favorite request DTO
     * @return a 204 No Content response
     */
    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(
            @Valid @RequestBody FavoriteRequestDTO request) {
        log.info("Removing Shop ID: {} from User ID: {} favorites", request.getShopId(), request.getUserId());
        favoriteService.removeFavorite(request.getUserId(), request.getShopId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Fetches all favorites for a user.
     *
     * @param userId the user ID
     * @return a list of favorite response DTOs
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<FavoriteResponseDTO>> getUserFavorites(@PathVariable UUID userId) {
        log.info("Fetching favorites for User ID: {}", userId);
        List<FavoriteResponseDTO> favorites = favoriteService.getFavoritesByUser(userId);
        return ResponseEntity.ok(favorites);
    }
}
