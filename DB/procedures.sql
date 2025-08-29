-- ========== User ==========
CREATE OR REPLACE PROCEDURE sp_create_user(
  p_user_id TEXT, p_user_name TEXT, p_email TEXT, p_password_hash TEXT, p_account TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO app_user(user_id, user_name, email, password_hash, account)
  VALUES (p_user_id, p_user_name, p_email, p_password_hash, p_account);
END;
$$;

CREATE OR REPLACE FUNCTION sp_get_user_by_email(
  p_email TEXT
) RETURNS TABLE(user_id TEXT, user_name TEXT, email TEXT, password_hash TEXT, account TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY SELECT u.user_id, u.user_name, u.email, u.password_hash, u.account
  FROM app_user u WHERE u.email = p_email;
END; $$;

-- ========== Product CRUD ==========
CREATE OR REPLACE FUNCTION sp_create_product(
  p_name TEXT, p_price NUMERIC, p_fee NUMERIC
) RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE v_no BIGINT;
BEGIN
  INSERT INTO product(product_name, price, fee_rate)
  VALUES (p_name, p_price, p_fee)
  RETURNING no INTO v_no;
  RETURN v_no;
END; $$;

CREATE OR REPLACE PROCEDURE sp_update_product(
  p_no BIGINT, p_name TEXT, p_price NUMERIC, p_fee NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
  UPDATE product SET product_name = p_name, price = p_price, fee_rate = p_fee
  WHERE no = p_no;
END; $$;

CREATE OR REPLACE PROCEDURE sp_delete_product(p_no BIGINT)
LANGUAGE plpgsql
AS $$
BEGIN
  DELETE FROM product WHERE no = p_no;
END; $$;

CREATE OR REPLACE FUNCTION sp_list_products()
RETURNS TABLE(no BIGINT, product_name TEXT, price NUMERIC, fee_rate NUMERIC)
LANGUAGE sql
AS $func$
  SELECT p.no, p.product_name, p.price, p.fee_rate
  FROM product AS p
  ORDER BY p.no;
$func$;

-- ========== Like（計算總金額、手續費） ==========
CREATE OR REPLACE FUNCTION sp_create_like(
  p_user_id TEXT, p_product_no BIGINT, p_account TEXT, p_quantity INT
) RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE v_price NUMERIC(18,2);
DECLARE v_fee_rate NUMERIC(6,4);
DECLARE v_total_fee NUMERIC(18,2);
DECLARE v_total_amount NUMERIC(18,2);
DECLARE v_sn BIGINT;
BEGIN
  SELECT price, fee_rate INTO v_price, v_fee_rate FROM product WHERE no = p_product_no;
  IF NOT FOUND THEN RAISE EXCEPTION 'Product not found %', p_product_no; END IF;

  v_total_fee := round(v_price * p_quantity * v_fee_rate, 2);
  v_total_amount := round(v_price * p_quantity + v_total_fee, 2);

  INSERT INTO like_list(user_id, product_no, quantity, account, total_fee, total_amount)
  VALUES (p_user_id, p_product_no, p_quantity, p_account, v_total_fee, v_total_amount)
  RETURNING sn INTO v_sn;

  RETURN v_sn;
END; $$;

CREATE OR REPLACE PROCEDURE sp_update_like(
  p_sn BIGINT, p_account TEXT, p_quantity INT
)
LANGUAGE plpgsql
AS $$
DECLARE v_price NUMERIC(18,2);
DECLARE v_fee_rate NUMERIC(6,4);
DECLARE v_product BIGINT;
DECLARE v_total_fee NUMERIC(18,2);
DECLARE v_total_amount NUMERIC(18,2);
BEGIN
  SELECT product_no INTO v_product FROM like_list WHERE sn = p_sn;
  IF NOT FOUND THEN RAISE EXCEPTION 'Like sn not found %', p_sn; END IF;

  SELECT price, fee_rate INTO v_price, v_fee_rate FROM product WHERE no = v_product;

  v_total_fee := round(v_price * p_quantity * v_fee_rate, 2);
  v_total_amount := round(v_price * p_quantity + v_total_fee, 2);

  UPDATE like_list
  SET account = p_account,
      quantity = p_quantity,
      total_fee = v_total_fee,
      total_amount = v_total_amount,
      updated_at = NOW()
  WHERE sn = p_sn;
END; $$;

CREATE OR REPLACE PROCEDURE sp_delete_like(p_sn BIGINT)
LANGUAGE plpgsql
AS $$
BEGIN
  DELETE FROM like_list WHERE sn = p_sn;
END; $$;

CREATE OR REPLACE FUNCTION sp_get_likes_by_user(
  p_user_id TEXT
) RETURNS TABLE(
  sn BIGINT, product_name TEXT, quantity INT, account TEXT,
  total_amount NUMERIC(18,2), total_fee NUMERIC(18,2), email TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY
  SELECT l.sn, p.product_name, l.quantity, l.account, l.total_amount, l.total_fee, u.email
  FROM like_list l
  JOIN product p ON p.no = l.product_no
  JOIN app_user u ON u.user_id = l.user_id
  WHERE l.user_id = p_user_id
  ORDER BY l.sn DESC;
END; $$;

-- ========== 審計紀錄 ==========
CREATE OR REPLACE PROCEDURE sp_create_audit(p_event TEXT, p_payload JSONB)
LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO audit_log(event, payload) VALUES (p_event, p_payload);
END; $$;
