package com.example.shopapp.controller;

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

    @PostMapping
    public ResponseEntity<Favorite> addFavorite(
            @RequestParam UUID userId,
            @RequestParam UUID shopId) {
        Favorite favorite = favoriteService.addFavorite(userId, shopId);
        return ResponseEntity.ok(favorite);
    }

    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(
            @RequestParam UUID userId,
            @RequestParam UUID shopId) {
        favoriteService.removeFavorite(userId, shopId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Favorite>> getUserFavorites(@PathVariable UUID userId) {
        List<Favorite> favorites = favoriteService.getFavoritesByUser(userId);
        return ResponseEntity.ok(favorites);
    }
}
