package com.shopapp.ShopService.service.impl;

import com.shopapp.ShopService.dto.*;
import com.shopapp.ShopService.dto.shop.request.ShopRegistrationRequest;
import com.shopapp.ShopService.dto.shop.response.ShopResponse;
import com.shopapp.ShopService.mapper.ShopMapper;
import com.shopapp.ShopService.model.*;
import com.shopapp.ShopService.repository.ShopRepository;
import com.shopapp.ShopService.service.*;
import com.shopapp.common.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopServiceImpl implements ShopService {
  private final ShopRepository shops;
  private final ShopMapper mapper;
  private final ShopAccess access;

  @Override
  public ShopResponse registerShop(
      UUID ownerId, ShopRegistrationRequest request, HttpServletRequest ignored) {
    Caller.role("SHOPKEEPER");
    Caller.owner(ownerId);
    if (shops.existsByOwnerId(ownerId))
      throw ApiException.conflict("This owner already has a shop");
    Shop shop = mapper.toEntity(request);
    shop.setOwnerId(ownerId);
    shop.setStatus(ShopStatus.PENDING);
    // The unsent approval flag is committed atomically with the shop, then delivered by the worker.
    return mapper.toResponse(shops.saveAndFlush(shop));
  }

  @Override
  @Transactional(readOnly = true)
  public ShopResponse getShopById(UUID id) {
    Shop shop = access.get(id);
    access.read(shop);
    return mapper.toResponse(shop);
  }

  @Override
  public ShopResponse updateShop(UUID id, UpdateShopRequest request) {
    Shop shop = access.get(id);
    access.edit(shop);
    mapper.updateEntity(shop, request);
    return mapper.toResponse(shops.saveAndFlush(shop));
  }

  @Override
  public ShopResponse toggleShopStatus(UUID id) {
    Shop shop = shops.locked(id).orElseThrow(() -> ApiException.notFound("Shop"));
    access.edit(shop);
    if (shop.getStatus() == ShopStatus.ACTIVE) shop.setStatus(ShopStatus.INACTIVE);
    else if (shop.getStatus() == ShopStatus.APPROVED || shop.getStatus() == ShopStatus.INACTIVE)
      shop.setStatus(ShopStatus.ACTIVE);
    else throw ApiException.conflict("Only an approved shop can change availability");
    return mapper.toResponse(shop);
  }

  public void applyDecision(UUID id, ShopStatus status) {
    applyDecision(id, status, 0);
  }

  public ShopResponse resubmit(UUID id) {
    var shop = access.getForUpdate(id);
    access.edit(shop);
    if (shop.getStatus() != ShopStatus.REJECTED)
      throw ApiException.conflict("Only a rejected application can be resubmitted");
    shop.setApprovalRevision(shop.getApprovalRevision() + 1);
    shop.setStatus(ShopStatus.PENDING);
    shop.setApprovalRequested(false);
    return mapper.toResponse(shops.saveAndFlush(shop));
  }

  public void applyDecision(UUID id, ShopStatus status, long revision) {
    Caller.role("SERVICE");
    Shop shop = shops.locked(id).orElseThrow(() -> ApiException.notFound("Shop"));
    if (revision < shop.getApprovalRevision()) return;
    if (revision != shop.getApprovalRevision())
      throw ApiException.conflict("Unknown approval revision");
    if (status != ShopStatus.APPROVED && status != ShopStatus.REJECTED)
      throw new IllegalArgumentException("Invalid approval decision");
    if (shop.getStatus() == status
        || (status == ShopStatus.APPROVED
            && (shop.getStatus() == ShopStatus.ACTIVE || shop.getStatus() == ShopStatus.INACTIVE)))
      return;
    if (shop.getStatus() != ShopStatus.PENDING)
      throw ApiException.conflict("Shop already has a different approval decision");
    shop.setStatus(status);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean doesShopExist(UUID id) {
    Shop shop = access.get(id);
    access.read(shop);
    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public ShopBasicInfoDTO getShopBasicInfo(UUID id) {
    Shop shop = access.get(id);
    access.read(shop);
    return mapper.toBasicInfo(shop);
  }

  @Override
  @Transactional(readOnly = true)
  public ShopResponse getShopByOwner(UUID ownerId) {
    if (!Caller.hasRole("ADMIN")) Caller.owner(ownerId);
    Shop shop = shops.findByOwnerId(ownerId);
    if (shop == null) throw ApiException.notFound("Shop");
    return mapper.toResponse(shop);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ShopResponse> getAllShops() {
    return search(0, 100, "", "").getContent();
  }

  @Transactional(readOnly = true)
  public Page<ShopResponse> search(int page, int size, String q, String category) {
    if (page < 0 || size < 1 || size > 100 || q.length() > 100 || category.length() > 60)
      throw new IllegalArgumentException("Invalid pagination or filters");
    boolean all = Caller.hasRole("ADMIN") || Caller.hasRole("SHOPKEEPER");
    UUID owner = Caller.hasRole("SHOPKEEPER") ? Caller.id() : null;
    return shops
        .search(
            all,
            List.of(ShopStatus.APPROVED, ShopStatus.ACTIVE, ShopStatus.INACTIVE),
            owner,
            category,
            q,
            PageRequest.of(page, size, Sort.by("name").and(Sort.by("id"))))
        .map(mapper::toResponse);
  }
}
