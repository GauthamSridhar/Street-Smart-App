package com.shopapp.UserService.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 254)
  private String email;

  @JsonIgnore
  @ToString.Exclude
  @Column(nullable = false)
  private String password;

  @Column(nullable = false, length = 100)
  private String fullName;

  @Column(nullable = false, unique = true, length = 16)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Column(nullable = false)
  private boolean verified;

  @Version private long version;
}
