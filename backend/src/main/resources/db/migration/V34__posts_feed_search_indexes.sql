CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_posts_general_title_trgm_active
    ON posts USING gin (lower(title) gin_trgm_ops)
    WHERE post_type = 'GENERAL'
      AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_posts_active_content_trgm
    ON posts USING gin (lower(content_raw) gin_trgm_ops)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_users_username_trgm
    ON users USING gin (lower(username) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_events_title_trgm
    ON events USING gin (lower(title) gin_trgm_ops);