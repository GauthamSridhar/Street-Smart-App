package com.shopapp.UserService.config;

import com.shopapp.UserService.model.*;
import com.shopapp.UserService.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AdminBootstrap {
  @Bean
  public ApplicationRunner bootstrapAdmin(
      UserRepository users,
      BCryptPasswordEncoder passwords,
      @Value("${ADMIN_EMAIL:}") String email,
      @Value("${ADMIN_PASSWORD:}") String password,
      @Value("${ADMIN_PHONE:}") String phone) {
    return args -> {
      if (email.isBlank() && password.isBlank() && phone.isBlank()) return;
      if (email.isBlank() || password.length() < 12 || phone.isBlank())
        throw new IllegalArgumentException(
            "Admin bootstrap requires email, phone and a password of at least 12 characters");
      String normalized = email.trim().toLowerCase(java.util.Locale.ROOT);
      if (users.findByEmail(normalized).isPresent()) return;
      var user = new User();
      user.setEmail(normalized);
      user.setPassword(passwords.encode(password));
      user.setPhoneNumber(phone);
      user.setFullName("Administrator");
      user.setRole(UserRole.ADMIN);
      users.save(user);
    };
  }
}
