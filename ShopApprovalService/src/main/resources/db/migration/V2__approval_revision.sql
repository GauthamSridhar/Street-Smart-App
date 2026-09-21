ALTER TABLE shop_approvals ADD COLUMN revision BIGINT NOT NULL DEFAULT 0;
CREATE TABLE approval_history (
 id UUID PRIMARY KEY, shop_id UUID NOT NULL, revision BIGINT NOT NULL,
 status VARCHAR(32) NOT NULL, reason VARCHAR(1000), decided_by UUID, decided_at TIMESTAMP,
 UNIQUE(shop_id, revision)
);
