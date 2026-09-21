CREATE TABLE shops (
 id UUID PRIMARY KEY, name VARCHAR(100) NOT NULL, description VARCHAR(1000) NOT NULL,
 category VARCHAR(60) NOT NULL, address VARCHAR(255) NOT NULL,
 latitude DOUBLE PRECISION NOT NULL CHECK (latitude BETWEEN -90 AND 90),
 longitude DOUBLE PRECISION NOT NULL CHECK (longitude BETWEEN -180 AND 180),
 owner_id UUID NOT NULL UNIQUE, status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING','APPROVED','REJECTED','ACTIVE','INACTIVE')),
 approval_requested BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX shops_status_category ON shops(status,category);
CREATE INDEX shops_pending_delivery ON shops(approval_requested);
CREATE TABLE products (
 id UUID PRIMARY KEY, name VARCHAR(255) NOT NULL, available BOOLEAN NOT NULL,
 shop_id UUID NOT NULL REFERENCES shops(id) ON DELETE CASCADE
);
CREATE INDEX products_shop ON products(shop_id);
CREATE TABLE images (
 id UUID PRIMARY KEY, file_name VARCHAR(255) NOT NULL,
 file_type VARCHAR(255) NOT NULL, file_size_in_bytes BIGINT NOT NULL,
 shop_id UUID NOT NULL REFERENCES shops(id) ON DELETE CASCADE
);
CREATE INDEX images_shop ON images(shop_id);

CREATE TABLE image_contents (
 image_id UUID PRIMARY KEY REFERENCES images(id) ON DELETE CASCADE,
 data BYTEA NOT NULL
);
