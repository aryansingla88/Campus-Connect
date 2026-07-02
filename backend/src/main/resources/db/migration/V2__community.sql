

-- INTERESTS
CREATE TABLE interests (
                           id SERIAL PRIMARY KEY,

                           label VARCHAR(255) UNIQUE NOT NULL,
                           category VARCHAR(100) NOT NULL
);


CREATE TABLE user_interests (
                                user_id INTEGER NOT NULL,
                                interest_id INTEGER NOT NULL,

                                PRIMARY KEY (user_id, interest_id),

                                CONSTRAINT fk_user_interests_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_user_interests_interest
                                    FOREIGN KEY (interest_id)
                                        REFERENCES interests(id)
                                        ON DELETE CASCADE
);


-- CLUBS
CREATE TABLE clubs (
                       id SERIAL PRIMARY KEY,

                       name VARCHAR(255) UNIQUE NOT NULL,
                       description TEXT,
                       logo_url VARCHAR(500),

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE club_members (
                              club_id INTEGER NOT NULL,
                              user_id INTEGER NOT NULL,

                              mem_status VARCHAR(20) NOT NULL DEFAULT 'pending',
                              role VARCHAR(20) NOT NULL DEFAULT 'member',

                              joined_at TIMESTAMP,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              PRIMARY KEY (club_id, user_id),

                              CONSTRAINT fk_club_members_club
                                  FOREIGN KEY (club_id)
                                      REFERENCES clubs(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_club_members_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);