CREATE TABLE campus_boundary_points (
                                        point_order INTEGER PRIMARY KEY,
                                        latitude DECIMAL(10,7) NOT NULL,
                                        longitude DECIMAL(10,7) NOT NULL
);


-----Update Presence Visibility

-- Convert BOOLEAN -> VARCHAR(20)
ALTER TABLE user_preferences
ALTER COLUMN show_presence TYPE VARCHAR(20)
USING (
    CASE
        WHEN show_presence THEN 'CONNECTIONS'
        ELSE 'OFF'
    END
);

-- Update default value
ALTER TABLE user_preferences
    ALTER COLUMN show_presence
        SET DEFAULT 'CONNECTIONS';

-- Restrict allowed values
ALTER TABLE user_preferences
    ADD CONSTRAINT chk_show_presence
        CHECK (
            show_presence IN (
                              'OFF',
                              'CONNECTIONS',
                              'CLUBS',
                              'COURSE',
                              'PUBLIC'
                )
            );