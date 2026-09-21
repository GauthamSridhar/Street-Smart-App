CREATE TABLE favorites (
 id UUID PRIMARY KEY, user_id UUID NOT NULL, shop_id UUID NOT NULL, shop_name VARCHAR(100) NOT NULL,
 CONSTRAINT favorites_user_shop UNIQUE(user_id,shop_id)
);
CREATE INDEX favorites_user ON favorites(user_id);
