package com.example.finpref.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.finpref.common.ApiResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController @RequestMapping("/auth") @Validated
public class AuthController {
  private final AuthService svc;
  public AuthController(AuthService svc){ this.svc = svc; }

  @Value("${app.cookie.name}") String COOKIE_NAME;
  @Value("${app.cookie.secure:false}") boolean COOKIE_SECURE;

  record RegisterReq(@NotBlank String userName, @Email String email, @Size(min=6) String password, @NotBlank String account){}
  record LoginReq(@Email String email, @NotBlank String password){}
  record TokenRes(String accessToken, String userId){}

  private void setRefreshCookie(HttpServletResponse res, String refresh){
    Cookie c = new Cookie(COOKIE_NAME, refresh);
    c.setHttpOnly(true); c.setSecure(COOKIE_SECURE); c.setPath("/");
    c.setMaxAge(60*60*24*7);
    c.setAttribute("SameSite","Lax");
    res.addCookie(c);
  }

  @PostMapping("/register")
  public ApiResponse<String> register(@RequestBody RegisterReq req){
    svc.register(req.userName(), req.email(), req.password(), req.account());
    return ApiResponse.ok("ok");
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokenRes>> login(@RequestBody LoginReq req, HttpServletResponse res){
    return svc.login(req.email(), req.password())
      .map(t -> {
        setRefreshCookie(res, t.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(new TokenRes(t.accessToken(), t.userId())));
      }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponse.err("invalid credentials")));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenRes>> refresh(@CookieValue(name="${app.cookie.name}", required=false) String refresh, HttpServletResponse res){
    if(refresh == null) return ResponseEntity.status(401).body(ApiResponse.err("no refresh"));
    return svc.refresh(refresh).map(t -> {
      setRefreshCookie(res, t.refreshToken());
      return ResponseEntity.ok(ApiResponse.ok(new TokenRes(t.accessToken(), t.userId())));
    }).orElseGet(() -> ResponseEntity.status(401).body(ApiResponse.err("invalid refresh")));
  }

  @PostMapping("/logout")
  public ApiResponse<String> logout(HttpServletResponse res){
    Cookie c = new Cookie(COOKIE_NAME, ""); c.setMaxAge(0); c.setHttpOnly(true); c.setSecure(true); c.setPath("/"); c.setAttribute("SameSite","Lax");
    res.addCookie(c);
    return ApiResponse.ok("ok");
  }
}
