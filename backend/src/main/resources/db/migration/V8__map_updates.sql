
-- Add visibility to POIs
ALTER TABLE poi
    ADD COLUMN visibility VARCHAR(50) NOT NULL DEFAULT 'PUBLIC';
-- PUBLIC | STUDENT | FACULTY | STAFF

-- Add icon type for frontend rendering
ALTER TABLE poi
    ADD COLUMN icon_type VARCHAR(50) NOT NULL DEFAULT 'default';
-- food | library | hostel | atm | sports | etc.

-- Ensure one shop is linked to exactly one POI
ALTER TABLE shops
    ADD CONSTRAINT uq_shops_poi_id UNIQUE (poi_id);