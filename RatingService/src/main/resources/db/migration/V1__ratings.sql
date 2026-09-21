CREATE TABLE ratings (
 id UUID PRIMARY KEY, user_id UUID NOT NULL, shop_id UUID NOT NULL,
 rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5), review VARCHAR(2000),
 created_at TIMESTAMP, updated_at TIMESTAMP, version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ratings_user_shop UNIQUE(user_id,shop_id)
);
CREATE INDEX ratings_shop ON ratings(shop_id);
