package com.example.finpref.auth;

import com.example.finpref.user.UserDao;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
  private final UserDao userDao; private final PasswordEncoder encoder; private final JwtUtil jwt;
  public AuthService(UserDao userDao, PasswordEncoder encoder, JwtUtil jwt){
    this.userDao = userDao; this.encoder = encoder; this.jwt = jwt;
  }

  public record Tokens(String accessToken, String refreshToken, String userId){}

  public void register(String userName, String email, String password, String account){
    String userId = UUID.randomUUID().toString();
    String hash = encoder.encode(password);
    userDao.create(userId, userName, email, hash, account);
  }

  public Optional<Tokens> login(String email, String password){
    Optional<Map<String,Object>> u = userDao.findByEmail(email);
    if(u.isEmpty()) return Optional.empty();
    String hash = (String) u.get().get("password_hash");
    if(!encoder.matches(password, hash)) return Optional.empty();
    String userId = (String) u.get().get("user_id");
    return Optional.of(new Tokens(jwt.genAccess(userId), jwt.genRefresh(userId), userId));
  }

  public Optional<Tokens> refresh(String refreshToken){
    try{
      String userId = jwt.parse(refreshToken).getSubject();
      return Optional.of(new Tokens(jwt.genAccess(userId), jwt.genRefresh(userId), userId));
    }catch(Exception e){ return Optional.empty(); }
  }
}
