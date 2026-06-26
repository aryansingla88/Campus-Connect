ALTER TABLE user_profile
    ADD COLUMN roll_number VARCHAR(20);

ALTER TABLE user_profile
    ADD CONSTRAINT uq_user_profile_roll_number
        UNIQUE (roll_number);