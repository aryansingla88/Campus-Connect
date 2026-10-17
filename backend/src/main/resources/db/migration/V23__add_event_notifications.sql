-- ============================================================
-- EVENT NOTIFICATION SUBSCRIPTIONS
-- ============================================================
-- Stores users who clicked "Notify Me" for an event.
-- A user can subscribe to an event only once.

CREATE TABLE event_notification_subscriptions (
                                                  id SERIAL PRIMARY KEY,

                                                  event_id INTEGER NOT NULL,
                                                  user_id INTEGER NOT NULL,

                                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                                  CONSTRAINT uq_event_notification_subscription
                                                      UNIQUE (event_id, user_id),

                                                  CONSTRAINT fk_event_notification_subscription_event
                                                      FOREIGN KEY (event_id)
                                                          REFERENCES events(id)
                                                          ON DELETE CASCADE,

                                                  CONSTRAINT fk_event_notification_subscription_user
                                                      FOREIGN KEY (user_id)
                                                          REFERENCES users(id)
                                                          ON DELETE CASCADE
);


-- ============================================================
-- NOTIFICATIONS
-- ============================================================
-- Persistent notification inbox for users.

CREATE TABLE notifications (
                               id SERIAL PRIMARY KEY,

                               user_id INTEGER NOT NULL,

                               type VARCHAR(50) NOT NULL,

                               title VARCHAR(255) NOT NULL,

                               message TEXT NOT NULL,

                               event_id INTEGER,

                               is_read BOOLEAN NOT NULL DEFAULT FALSE,

                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_notifications_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_notifications_event
                                   FOREIGN KEY (event_id)
                                       REFERENCES events(id)
                                       ON DELETE CASCADE
);


-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_event_notification_subscriptions_event
    ON event_notification_subscriptions(event_id);

CREATE INDEX idx_event_notification_subscriptions_user
    ON event_notification_subscriptions(user_id);

CREATE INDEX idx_notifications_user
    ON notifications(user_id);

CREATE INDEX idx_notifications_user_unread
    ON notifications(user_id, is_read);

CREATE INDEX idx_notifications_event
    ON notifications(event_id);