package com.shopapp.ShopService.service;

import com.shopapp.ShopService.dto.product.response.ProductSearchResult;
import com.shopapp.ShopService.model.ShopStatus;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductSearch {
  private final EntityManager entityManager;

  @Transactional(readOnly = true)
  public Page<ProductSearchResult> search(
      String q, String category, boolean availableOnly, int page, int size) {
    return search(q, category, availableOnly, page, size, null, null, "", null, null, null, false);
  }

  @Transactional(readOnly = true)
  public Page<ProductSearchResult> search(
      String q,
      String category,
      boolean availableOnly,
      int page,
      int size,
      java.math.BigDecimal minPrice,
      java.math.BigDecimal maxPrice,
      String currency,
      Double latitude,
      Double longitude,
      Double radiusKm,
      boolean fuzzy) {
    currency = currency == null ? "" : currency.trim().toUpperCase(Locale.ROOT);
    if ((minPrice != null || maxPrice != null) && currency.isEmpty()) currency = "INR";
    if (fuzzy && q.trim().length() < 3)
      throw new IllegalArgumentException("Typo-tolerant search needs at least three characters");
    if ((minPrice != null && minPrice.signum() < 0)
        || (maxPrice != null && maxPrice.signum() < 0)
        || (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
        || (!currency.isEmpty() && !currency.matches("[A-Z]{3}")))
      throw new IllegalArgumentException("Use a valid price range and currency");
    boolean nearby = latitude != null || longitude != null || radiusKm != null;
    if (nearby
        && (latitude == null
            || longitude == null
            || radiusKm == null
            || !Double.isFinite(latitude)
            || !Double.isFinite(longitude)
            || !Double.isFinite(radiusKm)
            || Math.abs(latitude) > 90
            || Math.abs(longitude) > 180
            || radiusKm <= 0
            || radiusKm > 100))
      throw new IllegalArgumentException("Supply coordinates and radius between zero and 100 km");
    if (q.length() > 100
        || category.length() > 60
        || page < 0
        || page > 10000
        || size < 1
        || size > 100)
      throw new IllegalArgumentException("Invalid search or pagination parameters");
    String pattern =
        "%"
            + q.trim()
                .toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
            + "%";
    String filter =
        " from Product p join p.shop s where s.status in :statuses"
            + " and (lower(p.name) like :pattern escape '!'"
            + (fuzzy
                ? " or cast(function('levenshtein_less_equal', lower(p.name), :needle, 2) as integer) <= 2)"
                : ")")
            + " and (:category = '' or lower(s.category) = :category)"
            + " and (:availableOnly = false or p.available = true)";
    var statuses = List.of(ShopStatus.APPROVED, ShopStatus.ACTIVE, ShopStatus.INACTIVE);
    var parameters = new java.util.HashMap<String, Object>();
    if (fuzzy) parameters.put("needle", q.trim().toLowerCase(Locale.ROOT));
    if (minPrice != null) {
      filter += " and p.price >= :minPrice";
      parameters.put("minPrice", minPrice);
    }
    if (maxPrice != null) {
      filter += " and p.price <= :maxPrice";
      parameters.put("maxPrice", maxPrice);
    }
    if (!currency.isEmpty()) {
      filter += " and p.currency = :currency";
      parameters.put("currency", currency);
    }
    String distance =
        "6371.0088 * acos(least(1.0, greatest(-1.0, sin(radians(s.latitude)) * sin(radians(:lat)) + cos(radians(s.latitude)) * cos(radians(:lat)) * cos(radians(s.longitude - :lon)))))";
    if (nearby) {
      filter += " and s.latitude between :minLat and :maxLat and " + distance + " <= :radius";
      parameters.put("lat", latitude);
      parameters.put("lon", longitude);
      parameters.put("radius", radiusKm);
      parameters.put("minLat", Math.max(-90, latitude - radiusKm / 111.0));
      parameters.put("maxLat", Math.min(90, latitude + radiusKm / 111.0));
    }
    var matches =
        entityManager.createQuery(
            "select new com.shopapp.ShopService.dto.product.response.ProductSearchResult("
                + "p.id,p.name,p.available,s.id,s.name,s.category,s.address,s.latitude,s.longitude,s.status,p.price,p.currency,p.updatedAt)"
                + filter
                + " order by "
                + (nearby ? distance + "," : "")
                + "lower(p.name),lower(s.name),p.id",
            ProductSearchResult.class);
    var count = entityManager.createQuery("select count(p)" + filter, Long.class);
    for (var query : List.of(matches, count)) {
      query.setParameter("statuses", statuses);
      query.setParameter("pattern", pattern);
      query.setParameter("category", category.trim().toLowerCase(Locale.ROOT));
      query.setParameter("availableOnly", availableOnly);
      parameters.forEach(query::setParameter);
    }
    return new PageImpl<>(
        matches.setFirstResult(page * size).setMaxResults(size).getResultList(),
        PageRequest.of(page, size),
        count.getSingleResult());
  }
}
