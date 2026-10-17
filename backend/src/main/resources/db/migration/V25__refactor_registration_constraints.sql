-----------------------------
-- REGISTRATION REFRACTOR CONSTRAINTS & INDEXES
-----------------------------

-- Ensure check constraints on event_registration_config
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_min_team_members'
    ) THEN
        ALTER TABLE event_registration_config
            ADD CONSTRAINT chk_min_team_members CHECK (min_team_members >= 1);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_max_team_members'
    ) THEN
        ALTER TABLE event_registration_config
            ADD CONSTRAINT chk_max_team_members CHECK (max_team_members >= min_team_members);
    END IF;
END $$;

-- Indexes for registration performance
CREATE INDEX IF NOT EXISTS idx_event_registration_fields_event ON event_registration_fields(event_id);
CREATE INDEX IF NOT EXISTS idx_event_registrations_event ON event_registrations(event_id);
CREATE INDEX IF NOT EXISTS idx_event_registrations_user ON event_registrations(user_id);
CREATE INDEX IF NOT EXISTS idx_event_registrations_team ON event_registrations(team_id);
