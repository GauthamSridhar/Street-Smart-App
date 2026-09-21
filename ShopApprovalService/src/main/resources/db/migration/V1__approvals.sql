CREATE TABLE shop_approvals (
 id UUID PRIMARY KEY, shop_id UUID NOT NULL UNIQUE, approval_status VARCHAR(32) NOT NULL CHECK (approval_status IN ('PENDING','APPROVED','REJECTED')),
 reason VARCHAR(1000), approved BOOLEAN NOT NULL DEFAULT FALSE, decided_by UUID, decided_at TIMESTAMP,
 synchronized_with_shop BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMP, updated_at TIMESTAMP,
 version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX approvals_pending ON shop_approvals(approval_status);
CREATE INDEX approvals_delivery ON shop_approvals(synchronized_with_shop);
