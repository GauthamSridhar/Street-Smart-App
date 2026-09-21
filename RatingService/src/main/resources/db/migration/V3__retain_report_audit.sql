-- Reports must survive both author deletion and administrator removal of a review.
-- Rebuild without a foreign key using SQL supported by PostgreSQL and the H2 test database.
CREATE TABLE retained_review_reports (
 id UUID PRIMARY KEY, rating_id UUID NOT NULL, reporter_id UUID NOT NULL,
 reason VARCHAR(500) NOT NULL, status VARCHAR(16) NOT NULL CHECK (status IN ('OPEN','DISMISSED','REMOVED')),
 created_at TIMESTAMP NOT NULL, resolved_at TIMESTAMP, resolved_by UUID,
 review_snapshot VARCHAR(2000) NOT NULL DEFAULT '', version BIGINT NOT NULL DEFAULT 0,
 UNIQUE(rating_id, reporter_id)
);
INSERT INTO retained_review_reports (id,rating_id,reporter_id,reason,status,created_at,resolved_at,resolved_by,review_snapshot)
 SELECT r.id,r.rating_id,r.reporter_id,r.reason,r.status,r.created_at,r.resolved_at,r.resolved_by,COALESCE(v.review,'')
 FROM review_reports r LEFT JOIN ratings v ON v.id=r.rating_id;
DROP TABLE review_reports;
ALTER TABLE retained_review_reports RENAME TO review_reports;
CREATE INDEX review_reports_open ON review_reports(status, created_at);
