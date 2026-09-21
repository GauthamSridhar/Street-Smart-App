package com.shopapp.FavoriteService.controller;

import com.shopapp.FavoriteService.dto.favourite.response.FavoriteResponseDTO;
import com.shopapp.FavoriteService.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

  private final FavoriteService favoriteService;

  @PostMapping("/{shopId}")
  public ResponseEntity<FavoriteResponseDTO> addFavorite(
      @RequestParam UUID userId, @PathVariable UUID shopId, HttpServletRequest request) {
    FavoriteResponseDTO response = favoriteService.addFavorite(userId, shopId, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{shopId}")
  public ResponseEntity<Void> removeFavorite(
      @RequestParam UUID userId, @PathVariable UUID shopId, HttpServletRequest request) {
    favoriteService.removeFavorite(userId, shopId, request);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<FavoriteResponseDTO>> getFavoritesByUser(
      @PathVariable UUID userId,
      HttpServletRequest request,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    List<FavoriteResponseDTO> favorites = favoriteService.page(userId, page, size);
    return ResponseEntity.ok(favorites);
  }

  @GetMapping("/{shopId}/is-favorite")
  public ResponseEntity<Boolean> isFavorite(@RequestParam UUID userId, @PathVariable UUID shopId) {
    boolean result = favoriteService.isFavorite(userId, shopId);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/count/{userId}")
  public ResponseEntity<Integer> getFavoriteCount(
      @PathVariable UUID userId, HttpServletRequest request) {
    int count = favoriteService.getFavoriteCount(userId, request);
    return ResponseEntity.ok(count);
  }
}
