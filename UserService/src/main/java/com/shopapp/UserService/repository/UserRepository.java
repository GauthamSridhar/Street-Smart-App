package com.shopapp.UserService.repository;

import com.shopapp.UserService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    @Query(value = "SELECT * FROM users u WHERE u.email = :email OR u.phone_number = :phoneNumber", nativeQuery = true)
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);


    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);
}
