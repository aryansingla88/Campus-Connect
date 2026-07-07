

-- Default all new registrations to CONFIRMED
ALTER TABLE event_registrations
    ALTER COLUMN status
        SET DEFAULT 'CONFIRMED';

-- Prevent duplicate registrations for the same event
ALTER TABLE event_registrations
    ADD CONSTRAINT uq_event_registration
        UNIQUE (event_id, user_id);

-- Restrict allowed registration statuses
ALTER TABLE event_registrations
    ADD CONSTRAINT chk_registration_status
        CHECK (
            status IN (
                       'PENDING',
                       'CONFIRMED',
                       'REJECTED',
                       'WAITLISTED'
                )
            );



--Improving Delete Condition
ALTER TABLE event_registration_answers
DROP CONSTRAINT fk_registration_answers_field;

ALTER TABLE event_registration_answers
    ADD CONSTRAINT fk_registration_answers_field
        FOREIGN KEY (field_id)
            REFERENCES event_registration_fields(id);