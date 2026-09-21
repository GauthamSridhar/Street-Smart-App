package com.shopapp.RatingService.service.impl;

import com.shopapp.RatingService.dto.rating.request.*;
import com.shopapp.RatingService.dto.rating.response.RatingResponseDTO;
import com.shopapp.RatingService.feign.ShopFeignClient;
import com.shopapp.RatingService.mapper.RatingMapper;
import com.shopapp.RatingService.repository.RatingRepository;
import com.shopapp.RatingService.repository.ReviewReportRepository;
import com.shopapp.RatingService.service.RatingService;
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
public class RatingServiceImpl implements RatingService {
  private final RatingRepository ratings;
  private final ReviewReportRepository reports;
  private final RatingMapper mapper;
  private final ShopFeignClient shops;

  public RatingResponseDTO addRating(
      UUID userId, UUID shopId, RatingCreateDTO request, HttpServletRequest ignored) {
    Caller.role("USER");
    Caller.owner(userId);
    var shop = shops.getShop(shopId);
    if (shop.getOwnerId().equals(userId))
      throw ApiException.conflict("You cannot rate your own shop");
    if (!Set.of("APPROVED", "ACTIVE", "INACTIVE").contains(shop.getStatus()))
      throw ApiException.conflict("Only approved shops can be rated");
    if (ratings.existsByUserIdAndShopId(userId, shopId))
      throw ApiException.conflict("Edit your existing rating for this shop");
    var rating = mapper.toEntity(request);
    rating.setUserId(userId);
    rating.setShopId(shopId);
    return mapper.toDTO(ratings.saveAndFlush(rating));
  }

  public RatingResponseDTO updateRating(UUID userId, UUID id, RatingUpdateDTO request) {
    Caller.role("USER");
    Caller.owner(userId);
    var rating = ratings.findById(id).orElseThrow(() -> ApiException.notFound("Rating"));
    Caller.owner(rating.getUserId());
    mapper.updateEntity(rating, request);
    return mapper.toDTO(ratings.saveAndFlush(rating));
  }

  public void deleteRating(UUID userId, UUID id, UUID shopId, HttpServletRequest ignored) {
    Caller.role("USER");
    Caller.owner(userId);
    var rating = ratings.findById(id).orElseThrow(() -> ApiException.notFound("Rating"));
    Caller.owner(rating.getUserId());
    if (!rating.getShopId().equals(shopId))
      throw new IllegalArgumentException("Rating does not belong to this shop");
    ratings.delete(rating);
  }

  @Transactional(readOnly = true)
  public List<RatingResponseDTO> getShopRatings(UUID shopId) {
    return getShopRatingsPage(shopId, 0, 100);
  }

  @Transactional(readOnly = true)
  public List<RatingResponseDTO> getShopRatingsPage(UUID shopId, int page, int size) {
    if (page < 0 || size < 1 || size > 100)
      throw new IllegalArgumentException("Invalid pagination");
    shops.getShop(shopId);
    return ratings
        .findByShopId(
            shopId,
            PageRequest.of(page, size, Sort.by("createdAt").descending().and(Sort.by("id"))))
        .stream()
        .map(mapper::toDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public RatingResponseDTO getRating(UUID id) {
    var rating = ratings.findById(id).orElseThrow(() -> ApiException.notFound("Rating"));
    shops.getShop(rating.getShopId());
    return mapper.toDTO(rating);
  }

  @Transactional(readOnly = true)
  public Long getShopRatingsCount(UUID shopId) {
    shops.getShop(shopId);
    return ratings.countByShopId(shopId);
  }

  @Transactional(readOnly = true)
  public Map<String, Number> summary(UUID shopId) {
    shops.getShop(shopId);
    return Map.of(
        "count", ratings.countByShopId(shopId), "average", ratings.averageByShopId(shopId));
  }

  public Map<String, Object> report(UUID ratingId, String reason) {
    Caller.role("USER");
    var rating = ratings.findById(ratingId).orElseThrow(() -> ApiException.notFound("Rating"));
    shops.getShop(rating.getShopId());
    if (rating.getUserId().equals(Caller.id()))
      throw ApiException.conflict("You cannot report your own review");
    if (reports.existsByRatingIdAndReporterId(ratingId, Caller.id()))
      throw ApiException.conflict("You already reported this review");
    var report = new com.shopapp.RatingService.model.ReviewReport();
    report.setId(UUID.randomUUID());
    report.setRatingId(ratingId);
    report.setReporterId(Caller.id());
    report.setReason(reason.trim());
    report.setReviewSnapshot(Objects.toString(rating.getReview(), ""));
    report.setStatus("OPEN");
    report.setCreatedAt(java.time.LocalDateTime.now());
    reports.saveAndFlush(report);
    return Map.of("id", report.getId(), "status", report.getStatus());
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> reports(String status) {
    Caller.role("ADMIN");
    if (!Set.of("OPEN", "DISMISSED", "REMOVED").contains(status))
      throw new IllegalArgumentException("Invalid report status");
    return reports.findByStatusOrderByCreatedAtAsc(status, PageRequest.of(0, 100)).stream()
        .map(
            r ->
                Map.<String, Object>of(
                    "id",
                    r.getId(),
                    "ratingId",
                    r.getRatingId(),
                    "reason",
                    r.getReason(),
                    "createdAt",
                    r.getCreatedAt(),
                    "status",
                    r.getStatus(),
                    "review",
                    r.getReviewSnapshot()))
        .toList();
  }

  public void resolveReport(UUID reportId, String action) {
    Caller.role("ADMIN");
    var report =
        reports.findById(reportId).orElseThrow(() -> ApiException.notFound("Review report"));
    if (!report.getStatus().equals("OPEN"))
      throw ApiException.conflict("Review report is already resolved");
    if (!Set.of("DISMISS", "REMOVE").contains(action))
      throw new IllegalArgumentException("Invalid moderation action");
    if (action.equals("REMOVE")) ratings.deleteById(report.getRatingId());
    report.setStatus(action.equals("REMOVE") ? "REMOVED" : "DISMISSED");
    report.setResolvedAt(java.time.LocalDateTime.now());
    report.setResolvedBy(Caller.id());
    reports.saveAndFlush(report);
  }
}
