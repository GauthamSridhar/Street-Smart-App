package com.shopapp.ShopService.repository;

import com.shopapp.ShopService.model.ImageContent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageContentRepository extends JpaRepository<ImageContent, UUID> {}
