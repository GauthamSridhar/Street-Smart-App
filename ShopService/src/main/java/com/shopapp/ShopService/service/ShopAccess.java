package com.shopapp.ShopService.service;

import com.shopapp.ShopService.model.*;
import com.shopapp.ShopService.repository.ShopRepository;
import com.shopapp.common.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShopAccess {
  private final ShopRepository shops;

  public Shop get(java.util.UUID id) {
    return shops.findById(id).orElseThrow(() -> ApiException.notFound("Shop"));
  }

  public Shop getForUpdate(java.util.UUID id) {
    return shops.locked(id).orElseThrow(() -> ApiException.notFound("Shop"));
  }

  public void edit(Shop shop) {
    Caller.role("SHOPKEEPER");
    Caller.owner(shop.getOwnerId());
  }

  public void read(Shop shop) {
    if (Caller.hasRole("SERVICE") || Caller.hasRole("ADMIN")) return;
    if (shop.getStatus() == ShopStatus.PENDING || shop.getStatus() == ShopStatus.REJECTED)
      Caller.owner(shop.getOwnerId());
  }
}
