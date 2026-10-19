ALTER TABLE event_registration_config
    ADD COLUMN auto_confirm BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE event_team_requests (
    id SERIAL PRIMARY KEY,
    team_id INTEGER NOT NULL,
    registration_id INTEGER NOT NULL,
    initiated_by VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event_team_requests_team
        FOREIGN KEY (team_id) REFERENCES event_teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_team_requests_registration
        FOREIGN KEY (registration_id) REFERENCES event_registrations(id) ON DELETE CASCADE,
    CONSTRAINT chk_event_team_requests_initiated_by
        CHECK (initiated_by IN ('PARTICIPANT', 'LEADER')),
    CONSTRAINT chk_event_team_requests_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED'))
);

CREATE UNIQUE INDEX uq_event_team_requests_pending_registration
    ON event_team_requests(registration_id)
    WHERE status = 'PENDING';

CREATE INDEX idx_event_team_requests_team_status_created
    ON event_team_requests(team_id, status, created_at);
