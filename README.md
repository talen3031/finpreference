# Fin Preference (fin-preference)

一個簡潔可維護的 **金融商品喜好系統**  
- **後端**：Java 21 + Spring Boot 3（REST API、JWT 驗證、CORS、Swagger/OpenAPI、HikariCP）  
- **前端**：Vite + Vue
- **資料庫**：PostgreSQL（Railway 公網代理）  

---

## 功能概要
- **產品管理（公開）**
  - `GET /products`：取得商品清單（已放行）
- **喜好清單（需登入）**
  - `POST /likes` 新增
  - `PUT /likes/{sn}` 修改
  - `DELETE /likes/{sn}` 刪除
  - `GET /likes` 取得目前使用者的喜好清單
- **認證（JWT）**
  - `POST /auth/register` 註冊
  - `POST /auth/login` 取得 `accessToken`（JSON）+ `refreshToken`（HttpOnly Cookie）
  - `POST /auth/refresh` 使用 Cookie 中 refresh 換取新 access
  - `POST /auth/logout` 清除 refresh Cookie
- **Swagger UI**：`/swagger-ui/index.html`
---

## 🗄️ 資料庫結構說明

本專案使用 **PostgreSQL**，資料表與關聯如下：

---

###  `app_user` – 使用者資料表

儲存註冊使用者基本資訊。

- `user_id`: 使用者唯一識別碼（主鍵）
- `user_name`: 使用者名稱
- `email`: 使用者電子郵件（唯一）
- `password_hash`: 加密後的密碼
- `account`: 預設帳戶名稱
- `created_at`: 建立時間（預設為 NOW）

---

###  `product` – 商品資料表

儲存可供選擇的金融商品資訊。

- `no`: 商品編號（主鍵，自動遞增）
- `product_name`: 商品名稱
- `price`: 商品價格，必須 ≥ 0
- `fee_rate`: 手續費比率，必須 ≥ 0

---

###  `like_list` – 喜好清單

儲存使用者加入的商品喜好紀錄。

- `sn`: 主鍵，自動遞增流水號
- `user_id`: 使用者 ID，對應 `app_user`
- `product_no`: 商品編號，對應 `product`
- `quantity`: 數量，必須 > 0
- `account`: 扣款帳號
- `total_fee`: 總手續費（價格 × 數量 × 手續費率）
- `total_amount`: 總金額（價格 × 數量）
- `created_at`: 建立時間
- `updated_at`: 最後更新時間
- **唯一鍵限制**：(`user_id`, `product_no`, `account`) 不可重複
- **索引**：`user_id`、`product_no` 各自建立索引以提升查詢效能



---