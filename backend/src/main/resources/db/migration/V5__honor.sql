
-- HONOR ITEMS(medal and badges)
CREATE TABLE honor_items (
                             id SERIAL PRIMARY KEY,

                             type VARCHAR(20) NOT NULL,        -- badges & medals

                             title VARCHAR(255) NOT NULL,      --auto for events
                             subtitle VARCHAR(255),            --Team name or {SOLO} for medals

                             icon_url VARCHAR(500),            -- Prefeded for conditions

                             event_id INTEGER,

                             condition VARCHAR(255),           -- medals(1st,2nd,3rd)
                                                               -- badges(predefined)

                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_honor_items_event
                                 FOREIGN KEY (event_id)
                                     REFERENCES events(id)
                                     ON DELETE SET NULL
);


-- USER HONOR(Mapping-solo based only)
CREATE TABLE user_honor (
                            user_id INTEGER NOT NULL,
                            honor_id INTEGER NOT NULL,

                            priority INTEGER NOT NULL DEFAULT 1,

                            awarded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            PRIMARY KEY (user_id, honor_id),

                            CONSTRAINT fk_user_honor_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_user_honor_honor
                                FOREIGN KEY (honor_id)
                                    REFERENCES honor_items(id)
                                    ON DELETE CASCADE
);



-- HONOR POINTS(Leaderboard rank)
CREATE TABLE honor_points (
                              user_id INTEGER PRIMARY KEY,

                              points INTEGER NOT NULL DEFAULT 0,

                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_honor_points_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);