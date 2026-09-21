package com.shopapp.UserService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SharedAuthQuota {
  private final JdbcTemplate jdbc;

  @Transactional
  public boolean allow(String path) {
    var rows =
        jdbc.queryForList(
            "select window_start,hits from auth_quotas where path=? for update", path);
    if (rows.isEmpty()) return true;
    long now = System.currentTimeMillis();
    var row = rows.get(0);
    long start = ((Number) row.get("window_start")).longValue();
    int hits = ((Number) row.get("hits")).intValue();
    if (now - start >= 60000) {
      start = now;
      hits = 0;
    }
    if (hits >= 30) return false;
    jdbc.update("update auth_quotas set window_start=?,hits=? where path=?", start, hits + 1, path);
    return true;
  }
}
