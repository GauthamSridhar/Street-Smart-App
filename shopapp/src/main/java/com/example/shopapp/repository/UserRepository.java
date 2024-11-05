// repository/UserRepository.java
package com.example.shopapp.repository;

import com.example.shopapp.model.User;
import com.example.shopapp.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByIdAndRole(UUID id, UserRole role);

}
