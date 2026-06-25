
-- USERS
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,

                       username VARCHAR(100) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,

                       role VARCHAR(50) NOT NULL DEFAULT 'student',
                       is_banned BOOLEAN NOT NULL DEFAULT FALSE,

                       last_login TIMESTAMP,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- COURSES
CREATE TABLE courses (
                         id SERIAL PRIMARY KEY,

                         course_name VARCHAR(255) UNIQUE NOT NULL,
                         duration_years INTEGER NOT NULL,
                         has_branch BOOLEAN NOT NULL DEFAULT FALSE,

                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- USER PROFILE
CREATE TABLE user_profile (
                              user_id INTEGER PRIMARY KEY,

                              full_name VARCHAR(255) NOT NULL,
                              bio TEXT,
                              avatar_url VARCHAR(500),

                              course_id INTEGER NOT NULL,
                              branch VARCHAR(255),
                              admission_year INTEGER NOT NULL,

                              hostel VARCHAR(255) NOT NULL,
                              hometown VARCHAR(255),

                              gender VARCHAR(50),
                              dob DATE,

                              phone VARCHAR(20),

                              github VARCHAR(255),
                              linkedin VARCHAR(255),
                              instagram VARCHAR(255),

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_profile_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_profile_course
                                  FOREIGN KEY (course_id)
                                      REFERENCES courses(id)
);


-- USER PREFERENCES
CREATE TABLE user_preferences (
                                  user_id INTEGER PRIMARY KEY,

                                  show_phone BOOLEAN NOT NULL DEFAULT FALSE,
                                  show_socials BOOLEAN NOT NULL DEFAULT TRUE,
                                  show_presence BOOLEAN NOT NULL DEFAULT TRUE,

                                  notify_connections BOOLEAN NOT NULL DEFAULT TRUE,
                                  notify_events BOOLEAN NOT NULL DEFAULT TRUE,
                                  notify_posts BOOLEAN NOT NULL DEFAULT TRUE,

                                  theme VARCHAR(20) NOT NULL DEFAULT 'system',

                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_preferences_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(id)
                                          ON DELETE CASCADE
);


-- USER FRIENDS
CREATE TABLE user_friends (
                              sender_id INTEGER NOT NULL,
                              receiver_id INTEGER NOT NULL,

                              status VARCHAR(20) NOT NULL,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              PRIMARY KEY (sender_id, receiver_id),

                              CONSTRAINT fk_friend_sender
                                  FOREIGN KEY (sender_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_friend_receiver
                                  FOREIGN KEY (receiver_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);