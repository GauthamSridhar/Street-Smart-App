ALTER TABLE products ADD COLUMN description VARCHAR(1000);
ALTER TABLE products ADD COLUMN price NUMERIC(12,2) CHECK (price >= 0);
ALTER TABLE products ADD COLUMN currency VARCHAR(3);
ALTER TABLE products ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE products ADD CONSTRAINT products_price_currency CHECK ((price IS NULL AND currency IS NULL) OR (price IS NOT NULL AND currency IS NOT NULL));
CREATE INDEX shops_coordinates ON shops(latitude, longitude);
