package com.shopapp.UserService.config;

import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      UserDetailsService users, BCryptPasswordEncoder passwords) {
    var provider = new DaoAuthenticationProvider(users);
    provider.setPasswordEncoder(passwords);
    return new ProviderManager(provider);
  }
}
