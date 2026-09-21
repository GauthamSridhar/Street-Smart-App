CREATE TABLE review_reports (
 id UUID PRIMARY KEY, rating_id UUID NOT NULL REFERENCES ratings(id) ON DELETE CASCADE,
 reporter_id UUID NOT NULL, reason VARCHAR(500) NOT NULL,
 status VARCHAR(16) NOT NULL CHECK (status IN ('OPEN','DISMISSED','REMOVED')),
 created_at TIMESTAMP NOT NULL, resolved_at TIMESTAMP, resolved_by UUID,
 CONSTRAINT review_report_once UNIQUE(rating_id, reporter_id)
);
CREATE INDEX review_reports_open ON review_reports(status, created_at);
