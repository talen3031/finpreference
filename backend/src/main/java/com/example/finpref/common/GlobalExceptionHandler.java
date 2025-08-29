package com.example.finpref.common;

import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    PSQLException psql = deepestPSQL(ex);
    if (psql != null && PSQLState.UNIQUE_VIOLATION.getState().equals(psql.getSQLState())) {
      String constraint = psql.getServerErrorMessage() != null
          ? psql.getServerErrorMessage().getConstraint()
          : null;

      if ("uq_like_user_product_account".equals(constraint)) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.err("此商品已在該帳號的喜好清單中"));
      }
    }

    // 其他資料完整性錯誤 → 回 400
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.err("資料不符合約束條件"));
  }

  /** 找出最深層的 PSQLException */
  private PSQLException deepestPSQL(Throwable t) {
    Throwable cur = t;
    PSQLException last = null;
    while (cur != null) {
      if (cur instanceof PSQLException p) last = p;
      cur = cur.getCause();
    }
    return last;
  }
}
