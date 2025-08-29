package com.example.finpref.config;

import com.example.finpref.auth.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

public class JwtFilter extends OncePerRequestFilter {
  private final JwtUtil jwt;
  public JwtFilter(JwtUtil jwt){ this.jwt = jwt; }

  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7);
      try {
        Claims c = jwt.parse(token);
        String userId = c.getSubject();
        var authToken = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authToken);
      } catch (Exception ignore) { /* invalid -> anonymous */ }
    }
    chain.doFilter(req, res);
  }
}
