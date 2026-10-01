CREATE TABLE user_account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_name VARCHAR(150) NOT NULL,
    login VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_user_account_display_name
        CHECK (display_name ~ '[^[:space:]]'),

    CONSTRAINT ck_user_account_login
        CHECK (
            login <> ''
            AND login = lower(login)
            AND login !~ '[[:space:]]'
        ),

    CONSTRAINT ck_user_account_email
        CHECK (
            email <> ''
            AND email !~ '[[:space:]]'
        ),

    CONSTRAINT ck_user_account_password_hash
        CHECK (password_hash ~ '[^[:space:]]'),

    CONSTRAINT uq_user_account_login UNIQUE (login)
);

CREATE UNIQUE INDEX uq_user_account_email_ignore_case
    ON user_account (lower(email));