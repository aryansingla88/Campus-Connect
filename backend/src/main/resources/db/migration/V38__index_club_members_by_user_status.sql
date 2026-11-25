CREATE INDEX IF NOT EXISTS idx_club_members_user_status_club
    ON club_members (user_id, mem_status, club_id);
