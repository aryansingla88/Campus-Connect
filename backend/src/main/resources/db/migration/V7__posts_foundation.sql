
-- POSTS
CREATE TABLE posts (
                       id SERIAL PRIMARY KEY,

                       creator_id INTEGER NOT NULL,

                       post_type VARCHAR(20) NOT NULL,

                       event_id INTEGER,
                       club_id INTEGER,
                       poi_id INTEGER,

                       title VARCHAR(255),

                       content_raw TEXT NOT NULL,
                       content_rendered TEXT,

                       visibility_type VARCHAR(50),
                       visibility_value VARCHAR(255),

                       allow_comments BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_posts_creator
                           FOREIGN KEY (creator_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE,

                       CONSTRAINT fk_posts_event
                           FOREIGN KEY (event_id)
                               REFERENCES events(id)
                               ON DELETE CASCADE,

                       CONSTRAINT fk_posts_club
                           FOREIGN KEY (club_id)
                               REFERENCES clubs(id)
                               ON DELETE SET NULL,

                       CONSTRAINT fk_posts_poi
                           FOREIGN KEY (poi_id)
                               REFERENCES poi(id)
                               ON DELETE SET NULL
);


-- POST TAGS
CREATE TABLE post_tags (
                           id SERIAL PRIMARY KEY,

                           name VARCHAR(100) UNIQUE NOT NULL
);


CREATE TABLE post_tag_map (
                              post_id INTEGER NOT NULL,
                              tag_id INTEGER NOT NULL,

                              PRIMARY KEY (post_id, tag_id),

                              CONSTRAINT fk_post_tag_map_post
                                  FOREIGN KEY (post_id)
                                      REFERENCES posts(id)
                                      ON DELETE CASCADE,

                              CONSTRAINT fk_post_tag_map_tag
                                  FOREIGN KEY (tag_id)
                                      REFERENCES post_tags(id)
                                      ON DELETE CASCADE
);


-- COMMENTS
CREATE TABLE comments (
                          id SERIAL PRIMARY KEY,

                          post_id INTEGER NOT NULL,
                          creator_id INTEGER NOT NULL,

                          parent_comment_id INTEGER,

                          content TEXT NOT NULL,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_comments_post
                              FOREIGN KEY (post_id)
                                  REFERENCES posts(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_comments_creator
                              FOREIGN KEY (creator_id)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_comments_parent
                              FOREIGN KEY (parent_comment_id)
                                  REFERENCES comments(id)
                                  ON DELETE CASCADE
);


-- POST VOTES
CREATE TABLE post_votes (
                            post_id INTEGER NOT NULL,
                            user_id INTEGER NOT NULL,

                            vote_type VARCHAR(10) NOT NULL,

                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            PRIMARY KEY (post_id, user_id),

                            CONSTRAINT fk_post_votes_post
                                FOREIGN KEY (post_id)
                                    REFERENCES posts(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_post_votes_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE
);




-- POST IMAGES
CREATE TABLE post_images (
                             id SERIAL PRIMARY KEY,

                             post_id INTEGER NOT NULL,

                             image_url VARCHAR(500) NOT NULL,

                             image_order INTEGER NOT NULL DEFAULT 1,

                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_post_images_post
                                 FOREIGN KEY (post_id)
                                     REFERENCES posts(id)
                                     ON DELETE CASCADE
);