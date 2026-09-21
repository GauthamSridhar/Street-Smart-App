package com.shopapp.common;

import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

public final class Caller {
  private Caller() {}

  public static boolean hasRole(String role) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null
        && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
  }

  public static UUID id() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    try {
      return UUID.fromString(auth.getName());
    } catch (RuntimeException ex) {
      throw new AccessDeniedException("A user identity is required");
    }
  }

  public static void owner(UUID owner) {
    if (!id().equals(owner))
      throw new AccessDeniedException("This resource belongs to another user");
  }

  public static void role(String role) {
    if (!hasRole(role)) throw new AccessDeniedException("Insufficient permissions");
  }
}
