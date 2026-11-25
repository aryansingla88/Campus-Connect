DROP INDEX IF EXISTS idx_posts_general_title_trgm_active;

CREATE INDEX idx_posts_general_title_trgm_active
    ON posts USING gin (lower(title) gin_trgm_ops)
    WHERE event_id IS NULL
      AND visibility_type IS NULL
      AND deleted_at IS NULL;

ALTER TABLE posts
    DROP CONSTRAINT IF EXISTS fk_posts_club,
    DROP CONSTRAINT IF EXISTS fk_posts_poi,
    DROP COLUMN IF EXISTS post_type,
    DROP COLUMN IF EXISTS club_id,
    DROP COLUMN IF EXISTS poi_id;
