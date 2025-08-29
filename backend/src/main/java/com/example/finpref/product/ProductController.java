package com.example.finpref.product;

import com.example.finpref.common.ApiResponse;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/products") @Validated
public class ProductController {
  private final ProductService svc;
  public ProductController(ProductService svc){ this.svc = svc; }

  record UpsertReq(@NotBlank String productName, @PositiveOrZero double price, @PositiveOrZero double feeRate){}

  @GetMapping public ApiResponse<List<ProductDao.Product>> list(){ return ApiResponse.ok(svc.list()); }

  // @PostMapping public ApiResponse<Long> create(@RequestBody UpsertReq req){
  //   return ApiResponse.ok(svc.create(req.productName(), req.price(), req.feeRate()));
  // }

  // @PutMapping("/{no}") public ApiResponse<String> update(@PathVariable long no, @RequestBody UpsertReq req){
  //   svc.update(no, req.productName(), req.price(), req.feeRate()); return ApiResponse.ok("ok");
  // }

  // @DeleteMapping("/{no}") public ApiResponse<String> delete(@PathVariable long no){ svc.delete(no); return ApiResponse.ok("ok"); }
}
