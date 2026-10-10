package com.shopapp.UserService.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken {
  @Id private String tokenHash;
  private UUID sessionId;
  private boolean consumed;
}
