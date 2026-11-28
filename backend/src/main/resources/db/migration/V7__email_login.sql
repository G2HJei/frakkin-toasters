-- The email is the account across its logins: a provider's verified email or the one a login code went to, lowercased.
-- The dev profile's users have none.
ALTER TABLE app_user
    ADD COLUMN email text UNIQUE;

-- The live login code of a user, hashed: a new one replaces it.
CREATE TABLE login_code
(
    user_id    uuid PRIMARY KEY REFERENCES app_user (id),
    code_hash  text        NOT NULL,
    created_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    attempts   integer     NOT NULL DEFAULT 0
);
