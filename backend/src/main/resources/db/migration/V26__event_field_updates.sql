-------------------------------------------
--        SOFT DELETE IN FIELDS
-------------------------------------------

ALTER TABLE event_registration_fields
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_event_registration_fields_active
    ON event_registration_fields(event_id)
    WHERE deleted_at IS NULL;