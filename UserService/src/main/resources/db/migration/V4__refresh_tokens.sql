CREATE TABLE refresh_tokens (
 token_hash VARCHAR(64) PRIMARY KEY,
 session_id UUID NOT NULL REFERENCES user_sessions(id) ON DELETE CASCADE,
 consumed BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX refresh_tokens_session ON refresh_tokens(session_id);
INSERT INTO auth_quotas(path) VALUES ('/api/users/refresh');
