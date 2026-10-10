package com.shopapp.UserService.repository;

import com.shopapp.UserService.model.UserSession;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<UserSession, UUID> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select s from UserSession s where s.id = :id")
  java.util.Optional<UserSession> locked(UUID id);

  void deleteByUserId(UUID userId);

  void deleteByExpiresAtBefore(Instant now);

  List<UserSession> findByUserIdAndExpiresAtAfterOrderByCreatedAtDesc(UUID userId, Instant now);
}
