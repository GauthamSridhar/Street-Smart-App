package com.shopapp.FavoriteService.service.impl;

import com.shopapp.FavoriteService.dto.favourite.response.FavoriteResponseDTO;
import com.shopapp.FavoriteService.feign.ShopFeignClient;
import com.shopapp.FavoriteService.mapper.FavoriteMapper;
import com.shopapp.FavoriteService.model.Favorite;
import com.shopapp.FavoriteService.repository.FavouriteRepository;
import com.shopapp.FavoriteService.service.FavoriteService;
import com.shopapp.common.Caller;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteServiceImpl implements FavoriteService {
  private final FavouriteRepository favorites;
  private final ShopFeignClient shops;
  private final FavoriteMapper mapper;

  private void owner(UUID id) {
    Caller.role("USER");
    Caller.owner(id);
  }

  public FavoriteResponseDTO addFavorite(UUID userId, UUID shopId, HttpServletRequest ignored) {
    owner(userId);
    var existing = favorites.findByUserIdAndShopId(userId, shopId);
    if (existing.isPresent()) return mapper.toDTO(existing.get(), existing.get().getShopName());
    var shop = shops.getShopBasicInfo(shopId);
    var favorite = new Favorite();
    favorite.setUserId(userId);
    favorite.setShopId(shopId);
    favorite.setShopName(shop.getName());
    return mapper.toDTO(favorites.saveAndFlush(favorite), shop.getName());
  }

  public void removeFavorite(UUID userId, UUID shopId, HttpServletRequest ignored) {
    owner(userId);
    favorites.deleteByUserIdAndShopId(userId, shopId);
  }

  @Transactional(readOnly = true)
  public List<FavoriteResponseDTO> getFavoritesByUser(UUID userId, HttpServletRequest ignored) {
    return page(userId, 0, 100);
  }

  @Transactional(readOnly = true)
  public List<FavoriteResponseDTO> page(UUID userId, int page, int size) {
    owner(userId);
    if (page < 0 || size < 1 || size > 100)
      throw new IllegalArgumentException("Invalid pagination");
    // The saved shop name is a display snapshot; favourites never depend on User Service
    // availability.
    return favorites.findByUserId(userId, PageRequest.of(page, size, Sort.by("id"))).stream()
        .map(f -> mapper.toDTO(f, f.getShopName()))
        .toList();
  }

  @Transactional(readOnly = true)
  public boolean isFavorite(UUID userId, UUID shopId) {
    owner(userId);
    return favorites.existsByUserIdAndShopId(userId, shopId);
  }

  @Transactional(readOnly = true)
  public int getFavoriteCount(UUID userId, HttpServletRequest ignored) {
    owner(userId);
    return Math.toIntExact(favorites.countByUserId(userId));
  }
}
