package com.shopapp.UserService.repository;

import com.shopapp.UserService.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from User u where u.id = :id")
  Optional<User> locked(UUID id);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from User u where u.email = :identifier or u.phoneNumber = :identifier")
  Optional<User> lockedByIdentifier(String identifier);

  @Query(
      value = "SELECT * FROM users u WHERE u.email = :email OR u.phone_number = :phoneNumber",
      nativeQuery = true)
  Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

  boolean existsByEmail(String email);

  boolean existsByPhoneNumber(String phoneNumber);

  Optional<User> findByEmail(String email);
}
