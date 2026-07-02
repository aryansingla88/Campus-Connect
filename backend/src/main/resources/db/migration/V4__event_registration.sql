
-- EVENT Questions
CREATE TABLE event_registration_fields (
                                           id SERIAL PRIMARY KEY,

                                           event_id INTEGER NOT NULL,

                                           field_label VARCHAR(255) NOT NULL,
                                           field_type VARCHAR(30) NOT NULL,

                                           is_required BOOLEAN NOT NULL DEFAULT FALSE,

                                           placeholder VARCHAR(255),

                                           field_order INTEGER NOT NULL,

                                           CONSTRAINT fk_registration_fields_event
                                               FOREIGN KEY (event_id)
                                                   REFERENCES events(id)
                                                   ON DELETE CASCADE
);


-- EVENT Questions OPTIONS(Dropdown etc)
CREATE TABLE event_registration_field_options (
                                                  id SERIAL PRIMARY KEY,

                                                  field_id INTEGER NOT NULL,

                                                  option_value VARCHAR(255) NOT NULL,
                                                  option_order INTEGER NOT NULL,

                                                  CONSTRAINT fk_registration_field_options_field
                                                      FOREIGN KEY (field_id)
                                                          REFERENCES event_registration_fields(id)
                                                          ON DELETE CASCADE
);



-- EVENT REGISTRATIONS(overall entry)
CREATE TABLE event_registrations (
                                     id SERIAL PRIMARY KEY,

                                     event_id INTEGER NOT NULL,
                                     user_id INTEGER NOT NULL,

                                     status VARCHAR(20) NOT NULL,

                                     submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_event_registrations_event
                                         FOREIGN KEY (event_id)
                                             REFERENCES events(id)
                                             ON DELETE CASCADE,

                                     CONSTRAINT fk_event_registrations_user
                                         FOREIGN KEY (user_id)
                                             REFERENCES users(id)
                                             ON DELETE CASCADE
);



-- EVENT REGISTRATION ANSWERS(each field)
CREATE TABLE event_registration_answers (
                                            registration_id INTEGER NOT NULL,
                                            field_id INTEGER NOT NULL,

                                            answer TEXT NOT NULL,

                                            PRIMARY KEY (registration_id, field_id),

                                            CONSTRAINT fk_registration_answers_registration
                                                FOREIGN KEY (registration_id)
                                                    REFERENCES event_registrations(id)
                                                    ON DELETE CASCADE,

                                            CONSTRAINT fk_registration_answers_field
                                                FOREIGN KEY (field_id)
                                                    REFERENCES event_registration_fields(id)
                                                    ON DELETE CASCADE
);