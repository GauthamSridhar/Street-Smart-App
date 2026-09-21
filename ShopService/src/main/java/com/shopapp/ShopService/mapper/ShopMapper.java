package com.shopapp.ShopService.mapper;

import com.shopapp.ShopService.dto.*;
import com.shopapp.ShopService.dto.shop.request.ShopRegistrationRequest;
import com.shopapp.ShopService.dto.shop.response.ShopResponse;
import com.shopapp.ShopService.model.Shop;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopMapper {
  private final ProductMapper products;
  private final ImageMapper images;

  public Shop toEntity(ShopRegistrationRequest r) {
    var s = new Shop();
    s.setName(r.getName().trim());
    s.setDescription(r.getDescription().trim());
    s.setAddress(r.getAddress().trim());
    s.setLatitude(r.getLatitude());
    s.setLongitude(r.getLongitude());
    s.setCategory(r.getCategory().trim());
    return s;
  }

  public void updateEntity(Shop s, UpdateShopRequest r) {
    s.setOpeningHours(r.getOpeningHours() == null ? null : r.getOpeningHours().trim());
    s.setName(r.getName().trim());
    s.setDescription(r.getDescription().trim());
    s.setAddress(r.getAddress().trim());
    s.setLatitude(r.getLatitude());
    s.setLongitude(r.getLongitude());
    s.setCategory(r.getCategory().trim());
  }

  public ShopResponse toResponse(Shop s) {
    var r = new ShopResponse();
    r.setId(s.getId());
    r.setName(s.getName());
    r.setDescription(s.getDescription());
    r.setAddress(s.getAddress());
    r.setLatitude(s.getLatitude());
    r.setLongitude(s.getLongitude());
    r.setStatus(s.getStatus());
    r.setCategory(s.getCategory());
    r.setOpeningHours(s.getOpeningHours());
    r.setOwnerId(s.getOwnerId());
    r.setProducts(s.getProducts().stream().map(products::toDTO).toList());
    r.setImages(s.getImages().stream().map(images::toDTO).toList());
    return r;
  }

  public ShopBasicInfoDTO toBasicInfo(Shop s) {
    return new ShopBasicInfoDTO(s.getId(), s.getName());
  }
}
