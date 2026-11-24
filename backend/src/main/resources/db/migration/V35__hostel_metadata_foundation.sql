CREATE TABLE hostels (
                         id SERIAL PRIMARY KEY,
                         hostel VARCHAR(10) NOT NULL UNIQUE,
                         name VARCHAR(100) NOT NULL UNIQUE,
                         active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO hostels (hostel, name, active) VALUES
                                               ('H1',  'Abhimanyu Bhawan', TRUE),
                                               ('H2',  'Bhishma Bhawan', TRUE),
                                               ('H3',  'Chakradhar Bhawan', TRUE),
                                               ('H4',  'Dronacharya Bhawan', TRUE),
                                               ('H5',  'Eklavya Bhawan', TRUE),
                                               ('H6',  'Fanibhushan Bhawan', TRUE),
                                               ('H7',  'Girivar Bhawan', TRUE),
                                               ('H8',  'Harihar Bhawan', TRUE),
                                               ('H9',  'Indivar Bhawan', TRUE),
                                               ('H10', 'Visvesvaraya Bhawan', TRUE),
                                               ('H11', 'Vivekananda Bhawan', TRUE),
                                               ('H12', 'Guest House', FALSE),
                                               ('GH1', 'Alaknanda Bhawan', TRUE),
                                               ('GH2', 'Bhagirathi Bhawan', TRUE),
                                               ('GH3', 'Cauvery Bhawan', TRUE),
                                               ('GH4', 'Kalpana Chawla Bhawan', TRUE),
                                               ('DS',  'Day Scholar', TRUE);

ALTER TABLE user_profile
    ADD COLUMN hostel_id INTEGER;

ALTER TABLE user_profile
    ADD CONSTRAINT fk_user_profile_hostel
        FOREIGN KEY (hostel_id) REFERENCES hostels(id);

CREATE INDEX idx_user_profile_hostel_id ON user_profile(hostel_id);

-- Backfill only matching canonical names (ignoring surrounding whitespace and case).
-- Unmatched legacy strings remain untouched in user_profile.hostel for review.
UPDATE user_profile profile
SET hostel_id = hostel.id
    FROM hostels hostel
WHERE profile.hostel IS NOT NULL
  AND lower(btrim(profile.hostel)) = lower(hostel.name);