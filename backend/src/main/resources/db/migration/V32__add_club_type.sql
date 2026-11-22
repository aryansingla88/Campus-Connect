ALTER TABLE clubs
    ADD COLUMN club_type VARCHAR(32) NOT NULL DEFAULT 'CLUB';

UPDATE clubs
SET club_type = 'Society'
WHERE id BETWEEN 1 AND 12;

UPDATE clubs
SET club_type = 'Student Club'
WHERE id BETWEEN 13 AND 23;
