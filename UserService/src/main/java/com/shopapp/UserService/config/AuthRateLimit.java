package com.shopapp.UserService.config;

import com.shopapp.UserService.service.impl.SharedAuthQuota;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(-105)
@RequiredArgsConstructor
public class AuthRateLimit extends OncePerRequestFilter {
  private final SharedAuthQuota quota;

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    String path = req.getRequestURI();
    if (req.getMethod().equals("POST")
        && (path.equals("/api/users/login")
            || path.equals("/api/users/register")
            || path.equals("/api/sms/send")
            || path.equals("/api/sms/verify"))) {
      // One conservative quota per endpoint, shared by all replicas through PostgreSQL.
      boolean allowed;
      try {
        allowed = quota.allow(path);
      } catch (RuntimeException e) {
        res.sendError(503);
        return;
      }
      if (!allowed) {
        res.setStatus(429);
        res.setHeader("Retry-After", "60");
        res.setContentType("application/problem+json");
        res.getWriter().write("{\"status\":429,\"detail\":\"Too many requests; try again later\"}");
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
