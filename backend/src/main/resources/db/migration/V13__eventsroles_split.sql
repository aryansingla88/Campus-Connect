-- Remove team assignment from event management
ALTER TABLE event_members
DROP CONSTRAINT fk_event_members_team;

ALTER TABLE event_members
DROP COLUMN team_id;


-- Add team assignment to event registrations
ALTER TABLE event_registrations
    ADD COLUMN team_id INTEGER;

ALTER TABLE event_registrations
    ADD CONSTRAINT fk_event_registrations_team
        FOREIGN KEY (team_id)
            REFERENCES event_teams(id)
            ON DELETE SET NULL;