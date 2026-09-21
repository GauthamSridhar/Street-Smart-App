package com.shopapp.common;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(-110)
public class RequestTracing extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    String supplied = req.getHeader("X-Request-ID");
    String id =
        supplied != null && supplied.matches("[a-fA-F0-9-]{36}")
            ? supplied
            : UUID.randomUUID().toString();
    req.setAttribute("requestId", id);
    res.setHeader("X-Request-ID", id);
    try (var ignored = MDC.putCloseable("requestId", id)) {
      chain.doFilter(req, res);
    }
  }
}
