
-- POINTS OF INTEREST
CREATE TABLE poi (
                     id SERIAL PRIMARY KEY,

                     name VARCHAR(255) NOT NULL,

                     category VARCHAR(100) NOT NULL,      --SHOP & Others

                     description TEXT,

                     latitude DOUBLE PRECISION,
                     longitude DOUBLE PRECISION,

                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- SHOPS
CREATE TABLE shops (
                       id SERIAL PRIMARY KEY,

                       poi_id INTEGER NOT NULL,

                       opening_time TIME,
                       closing_time TIME,

                       phone_number VARCHAR(20),

                       CONSTRAINT fk_shops_poi
                           FOREIGN KEY (poi_id)
                               REFERENCES poi(id)
                               ON DELETE CASCADE
);


-- USER PRESENCE
CREATE TABLE user_presence (
                               user_id INTEGER PRIMARY KEY,

                               latitude DOUBLE PRECISION,
                               longitude DOUBLE PRECISION,

                               last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_user_presence_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
);