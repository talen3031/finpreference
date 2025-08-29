package com.example.finpref.common;
public record ApiResponse<T>(boolean ok, T data, String error){
  public static <T> ApiResponse<T> ok(T d){ return new ApiResponse<>(true, d, null); }
  public static <T> ApiResponse<T> err(String e){ return new ApiResponse<>(false, null, e); }
}
