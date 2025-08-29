package com.example.finpref.like;


// src/main/java/com/example/finpref/like/LikeController.java
import com.example.finpref.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/likes")
@SecurityRequirement(name = "bearerAuth")   // ★ 新增這行
public class LikeController {

  private final LikeService svc;

  public LikeController(LikeService svc) {
    this.svc = svc;
  }

  // ------- DTOs -------
  public record CreateReq(
      @NotNull Long productNo,
      @NotBlank String account,
      @Min(1) int quantity
  ) {}

  public record UpdateReq(
      @NotBlank String account,
      @Min(1) int quantity
  ) {}

  // ------- Endpoints -------

  // 新增喜好（需登入）
  @PostMapping
  public ApiResponse<Long> create(@RequestBody @Valid CreateReq req, Authentication auth) {
    String userId = auth.getName(); // JwtFilter 已把 userId 放進 Authentication
    long sn = svc.create(userId, req.productNo(), req.account(), req.quantity());
    return ApiResponse.ok(sn);
  }
  // 修改既有喜好（需登入）
  @PutMapping("/{sn}")
  public ApiResponse<String> update(
      @PathVariable("sn") long sn,          // ← 關鍵：指定路徑變數名稱
      @RequestBody @Valid UpdateReq req,
      Authentication auth
  ) {
    // 如需檢查擁有者，可在 Service 內加上 userId 驗證；此處先依你現有 Service 介面呼叫
    svc.update(sn, req.account(), req.quantity());
    return ApiResponse.ok("ok");
  }

  // 刪除喜好（需登入）
  @DeleteMapping("/{sn}")
  public ApiResponse<String> delete(
      @PathVariable("sn") long sn,          // ← 關鍵：指定路徑變數名稱
      Authentication auth
  ) {
    svc.delete(sn);
    return ApiResponse.ok("ok");
  }

  // 取得目前使用者的喜好清單（需登入）
  @GetMapping
  public ApiResponse<List<Map<String, Object>>> list(Authentication auth) {
    String userId = auth.getName();
    return ApiResponse.ok(svc.listByUser(userId));
  }
}
