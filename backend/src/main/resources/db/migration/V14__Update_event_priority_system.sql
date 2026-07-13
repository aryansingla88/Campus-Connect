-- Remove old priority column
ALTER TABLE events
DROP COLUMN priority;

-- Add new priority system
ALTER TABLE events
ADD COLUMN base_priority_level INTEGER NOT NULL DEFAULT 1,
ADD COLUMN priority_score DOUBLE PRECISION NOT NULL DEFAULT 0,
ADD COLUMN priority_level INTEGER NOT NULL DEFAULT 1;

-- Indexes
CREATE INDEX idx_events_priority_score
    ON events(priority_score DESC);

CREATE INDEX idx_events_priority_level
    ON events(priority_level);

CREATE INDEX idx_events_start_time
    ON events(start_time);