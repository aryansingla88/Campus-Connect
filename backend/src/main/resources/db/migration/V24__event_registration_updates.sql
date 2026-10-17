
-----------------------------
--     NEW CONFIG TABLE AND TEAM UPDATES
-----------------------------

CREATE TABLE event_registration_config (
                                           event_id INT PRIMARY KEY REFERENCES events(id),
                                           min_team_members INT NOT NULL,
                                           max_team_members INT NOT NULL,
                                           quick_field_mask BIGINT NOT NULL DEFAULT 0,
                                           registration_created_at TIMESTAMP,
                                           registration_end_at TIMESTAMP
);

ALTER TABLE event_registration_fields
    ADD COLUMN individual BOOLEAN NOT NULL DEFAULT TRUE;