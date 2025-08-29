package com.example.finpref.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {
  private final Key key;
  private final int accessMinutes;
  private final int refreshDays;

  public JwtUtil(
    @Value("${app.jwt.secret}") String secret,
    @Value("${app.jwt.accessMinutes}") int accessMinutes,
    @Value("${app.jwt.refreshDays}") int refreshDays
  ){
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.accessMinutes = accessMinutes;
    this.refreshDays = refreshDays;
  }

  public String genAccess(String userId){
    Instant now = Instant.now();
    return Jwts.builder().setSubject(userId).setIssuedAt(Date.from(now))
      .setExpiration(Date.from(now.plusSeconds(accessMinutes * 60L)))
      .signWith(key, SignatureAlgorithm.HS256).compact();
  }

  public String genRefresh(String userId){
    Instant now = Instant.now();
    return Jwts.builder().setSubject(userId).setIssuedAt(Date.from(now))
      .setExpiration(Date.from(now.plusSeconds(refreshDays * 24L * 3600L)))
      .signWith(key, SignatureAlgorithm.HS256).compact();
  }

  public Claims parse(String token){ 
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody(); 
  }
}
