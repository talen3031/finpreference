-- PostgreSQL 14+
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS app_user (
  user_id TEXT PRIMARY KEY,
  user_name TEXT NOT NULL,
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  account TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS product (
  no BIGSERIAL PRIMARY KEY,
  product_name TEXT NOT NULL,
  price NUMERIC(18,2) NOT NULL CHECK (price >= 0),
  fee_rate NUMERIC(6,4) NOT NULL CHECK (fee_rate >= 0)
);

CREATE TABLE IF NOT EXISTS like_list (
  sn BIGSERIAL PRIMARY KEY,
  user_id TEXT NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  product_no BIGINT NOT NULL REFERENCES product(no) ON DELETE RESTRICT,
  quantity INT NOT NULL CHECK (quantity > 0),
  account TEXT NOT NULL,
  total_fee NUMERIC(18,2) NOT NULL,
  total_amount NUMERIC(18,2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_like_user_product_account UNIQUE (user_id, product_no, account)
);

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGSERIAL PRIMARY KEY,
  event TEXT NOT NULL,
  payload JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS account (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id TEXT NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
  account TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'active'
         CHECK (status IN ('active', 'disabled')),

  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  CONSTRAINT uq_user_account UNIQUE (user_id, account)
);
CREATE INDEX IF NOT EXISTS idx_like_user ON like_list(user_id);
CREATE INDEX IF NOT EXISTS idx_like_product ON like_list(product_no);
