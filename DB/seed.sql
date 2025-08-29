-- demo 使用者（密碼 test123 的 bcrypt 示例：請以實際註冊 API 產生者為準）
CALL sp_create_user(
  'd524efd9-13a6-4272-beb1-727e02540bbc','test','test@email.com',
  '$2a$10$sK5pUs8ipsDrV6WhzvdSbOxj11e7YgP9UUfCLSjBqg/c6hTXkLaC2',
  '1111999666'
);

-- demo 產品（這兩個仍然是 FUNCTION，有回傳值，所以維持 SELECT）
-- ETF
SELECT sp_create_product('ETF 0056 高股息', 38.50, 0.002);   -- 0.2%
SELECT sp_create_product('ETF 0050', 150.00, 0.003);    -- 0.3%
SELECT sp_create_product('ETF 006208 富邦台50', 73.20, 0.0015); -- 0.15%
SELECT sp_create_product('ETF 00757 群益中國5G', 15.80, 0.0025); -- 0.25%
-- 債券
SELECT sp_create_product('美國10年期公債', 100.00, 0.0008);
SELECT sp_create_product('公司債-AA級', 98.75, 0.0010);
-- 基金
SELECT sp_create_product('富蘭克林科技基金', 45.70, 0.0150);  -- 1.5%
SELECT sp_create_product('摩根全球債券基金', 32.10, 0.0100);  -- 1.0%
SELECT sp_create_product('美股-微軟', 430.00, 0.0025); -- 0.25%

-- 期貨
SELECT sp_create_product('台指期近月', 17250.00, 0.0006);
SELECT sp_create_product('美原油期貨 WTI', 79.20, 0.0008);

-- 外匯
SELECT sp_create_product('美元/台幣 USD/TWD', 32.10, 0.0005);
SELECT sp_create_product('歐元/美元 EUR/USD', 1.0950, 0.0005);
SELECT sp_create_product('日圓/台幣 JPY/TWD', 0.215, 0.0005);

