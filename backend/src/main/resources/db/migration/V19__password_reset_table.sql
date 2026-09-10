CREATE TABLE password_reset_tokens (

    id SERIAL PRIMARY KEY,

    user_id INTEGER NOT NULL,

    otp_hash VARCHAR(255) NOT NULL,

    expires_at TIMESTAMP NOT NULL,

    attempt_count INTEGER NOT NULL DEFAULT 0,

    otp_verified_at TIMESTAMP,

    reset_token_hash VARCHAR(255),

    reset_token_expires_at TIMESTAMP,

    used_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_user_id
ON password_reset_tokens(user_id);

CREATE INDEX idx_password_reset_tokens_expires_at
ON password_reset_tokens(expires_at);

