// SecurityConfig.java
package com.example.finpref.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.finpref.auth.JwtUtil;

@Configuration
public class SecurityConfig {

  @Value("${app.cors.allowedOrigin:http://localhost:5173}")
  private String allowedOrigin;

  @Bean
  PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwt) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .cors(c -> c.configurationSource(corsConfigurationSource()))
      .authorizeHttpRequests(reg -> reg
        // CORS 預檢
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        // Swagger & OpenAPI
        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
        // Auth
        .requestMatchers("/auth/**").permitAll()
        // ✅ 放行商品列表（同時放行 /products 與 /products/**）
        .requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
        // 把 /error 放行，避免未來任何未捕捉例外又變成 403 誤導
        .requestMatchers("/error","/errors").permitAll()
        // 其餘需要登入
        .anyRequest().authenticated()
      )
      .addFilterBefore(new JwtFilter(jwt), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    // 允許全部來源（AllowedOriginPatterns 可與 allowCredentials(true) 同用）
    cfg.setAllowedOriginPatterns(List.of("*"));
    // 白名單而非萬用，改成：
    // cfg.setAllowedOrigins(List.of(allowedOrigin, "http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:8080"));

    cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setExposedHeaders(List.of("Authorization"));
    cfg.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/**", cfg);
    return src;
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration ac) throws Exception {
    return ac.getAuthenticationManager();
  }
}
