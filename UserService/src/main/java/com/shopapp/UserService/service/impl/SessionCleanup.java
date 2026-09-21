package com.shopapp.UserService.service.impl;

import com.shopapp.UserService.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionCleanup {
  private final SessionRepository sessions;

  @Scheduled(fixedDelay = 3600000, initialDelay = 3600000)
  @Transactional
  public void cleanup() {
    sessions.deleteByExpiresAtBefore(java.time.Instant.now());
  }
}
