CREATE TABLE user_sessions (id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, expires_at TIMESTAMP WITH TIME ZONE NOT NULL);
CREATE INDEX user_sessions_owner ON user_sessions(user_id);
CREATE INDEX user_sessions_expiry ON user_sessions(expires_at);
CREATE TABLE auth_quotas (path VARCHAR(100) PRIMARY KEY, window_start BIGINT NOT NULL DEFAULT 0, hits INTEGER NOT NULL DEFAULT 0);
INSERT INTO auth_quotas(path) VALUES ('/api/users/login'),('/api/users/register'),('/api/sms/send'),('/api/sms/verify');
