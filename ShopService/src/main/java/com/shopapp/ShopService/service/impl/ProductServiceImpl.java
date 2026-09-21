package com.shopapp.ShopService.service.impl;

import com.shopapp.ShopService.dto.product.request.AddProductRequest;
import com.shopapp.ShopService.dto.product.response.ProductResponseDTO;
import com.shopapp.ShopService.mapper.ProductMapper;
import com.shopapp.ShopService.repository.ProductRepository;
import com.shopapp.ShopService.service.*;
import com.shopapp.common.ApiException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
  private final ProductRepository products;
  private final ProductMapper mapper;
  private final ShopAccess access;

  public ProductResponseDTO addProduct(UUID shopId, AddProductRequest request) {
    var shop = access.getForUpdate(shopId);
    access.edit(shop);
    if (products.countByShopId(shopId) >= 200)
      throw ApiException.conflict("A shop can have at most 200 products");
    var product = mapper.toEntity(request);
    product.setShop(shop);
    return mapper.toDTO(products.saveAndFlush(product));
  }

  public ProductResponseDTO updateProduct(UUID id, AddProductRequest request) {
    var product = products.findById(id).orElseThrow(() -> ApiException.notFound("Product"));
    access.edit(product.getShop());
    mapper.updateEntity(product, request);
    return mapper.toDTO(products.saveAndFlush(product));
  }

  public void deleteProduct(UUID id) {
    var product = products.findById(id).orElseThrow(() -> ApiException.notFound("Product"));
    access.edit(product.getShop());
    products.delete(product);
  }

  @Transactional(readOnly = true)
  public List<ProductResponseDTO> getProductsByShop(UUID id) {
    access.read(access.get(id));
    return products.findByShopId(id).stream().map(mapper::toDTO).toList();
  }

  @Transactional(readOnly = true)
  public ProductResponseDTO getProductById(UUID id) {
    var product = products.findById(id).orElseThrow(() -> ApiException.notFound("Product"));
    access.read(product.getShop());
    return mapper.toDTO(product);
  }

  @Transactional(readOnly = true)
  public Long getProductCount(UUID id) {
    access.read(access.get(id));
    return products.countByShopId(id);
  }
}
