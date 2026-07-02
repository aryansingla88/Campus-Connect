
-- EVENTS
CREATE TABLE events (
                        id SERIAL PRIMARY KEY,

                        title VARCHAR(255) NOT NULL,
                        description TEXT,

                        creator_id INTEGER NOT NULL,
                        club_id INTEGER,

                        host_name VARCHAR(255),
                        venue VARCHAR(255),

                        latitude DOUBLE PRECISION,
                        longitude DOUBLE PRECISION,

                        start_time TIMESTAMP NOT NULL,
                        end_time TIMESTAMP,

                        visibility_type VARCHAR(20) NOT NULL,
                        visibility_value VARCHAR(255),

                        registration_type VARCHAR(20) NOT NULL,
                        registration_link VARCHAR(500),

                        approval_status VARCHAR(20) NOT NULL,
                        event_state VARCHAR(20) NOT NULL,

                        priority INTEGER NOT NULL,

                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_events_creator
                            FOREIGN KEY (creator_id)
                                REFERENCES users(id)
                                ON DELETE CASCADE,

                        CONSTRAINT fk_events_club
                            FOREIGN KEY (club_id)
                                REFERENCES clubs(id)
                                ON DELETE SET NULL
);

-- Posters
CREATE TABLE event_posters (
                               id SERIAL PRIMARY KEY,

                               event_id INTEGER NOT NULL,

                               poster_url VARCHAR(500) NOT NULL,

                               uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_event_posters_event
                                   FOREIGN KEY (event_id)
                                       REFERENCES events(id)
                                       ON DELETE CASCADE
);

-- Categories and mapping
CREATE TABLE event_categories (
                                  id SERIAL PRIMARY KEY,

                                  name VARCHAR(100) UNIQUE NOT NULL
);


CREATE TABLE event_category_map (
                                    event_id INTEGER NOT NULL,
                                    category_id INTEGER NOT NULL,

                                    PRIMARY KEY (event_id, category_id),

                                    CONSTRAINT fk_event_category_map_event
                                        FOREIGN KEY (event_id)
                                            REFERENCES events(id)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_event_category_map_category
                                        FOREIGN KEY (category_id)
                                            REFERENCES event_categories(id)
                                            ON DELETE CASCADE
);

-- Teams
CREATE TABLE event_teams (
                             id SERIAL PRIMARY KEY,

                             event_id INTEGER NOT NULL,

                             team_name VARCHAR(255) NOT NULL,

                             leader_id INTEGER NOT NULL,

                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_event_teams_event
                                 FOREIGN KEY (event_id)
                                     REFERENCES events(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_event_teams_leader
                                 FOREIGN KEY (leader_id)
                                     REFERENCES users(id)
                                     ON DELETE CASCADE
);

-- Role based members
CREATE TABLE event_members (
                               event_id INTEGER NOT NULL,
                               user_id INTEGER NOT NULL,

                               role VARCHAR(20) NOT NULL,

                               team_id INTEGER,

                               joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               PRIMARY KEY (event_id, user_id),

                               CONSTRAINT fk_event_members_event
                                   FOREIGN KEY (event_id)
                                       REFERENCES events(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_event_members_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_event_members_team
                                   FOREIGN KEY (team_id)
                                       REFERENCES event_teams(id)
                                       ON DELETE SET NULL
);