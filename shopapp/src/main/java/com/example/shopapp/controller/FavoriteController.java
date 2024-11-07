package com.example.shopapp.controller;

import com.example.shopapp.dto.request.FavoriteRequestDTO;
import com.example.shopapp.dto.response.FavoriteResponseDTO;
import com.example.shopapp.mapper.FavoriteMapper;
import com.example.shopapp.model.Favorite;
import com.example.shopapp.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final FavoriteMapper favoriteMapper;

    @PostMapping
    public ResponseEntity<FavoriteResponseDTO> addFavorite(
            @Valid @RequestBody FavoriteRequestDTO request) {
        Favorite favorite = favoriteService.addFavorite(request.getUserId(), request.getShopId());
        return ResponseEntity.ok(favoriteMapper.toDTO(favorite));
    }

    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(
            @Valid @RequestBody FavoriteRequestDTO request) {
        favoriteService.removeFavorite(request.getUserId(), request.getShopId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<FavoriteResponseDTO>> getUserFavorites(
            @PathVariable UUID userId) {
        List<Favorite> favorites = favoriteService.getFavoritesByUser(userId);
        return ResponseEntity.ok(favoriteMapper.toDTOList(favorites));
    }

    @GetMapping("/users/{userId}/shops/{shopId}/exists")
    public ResponseEntity<Boolean> isFavorite(
            @PathVariable UUID userId,
            @PathVariable UUID shopId) {
        boolean exists = favoriteService.isFavorite(userId, shopId);
        return ResponseEntity.ok(exists);
    }
}
