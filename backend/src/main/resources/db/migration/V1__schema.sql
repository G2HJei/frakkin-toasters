CREATE TABLE app_user
(
    id       uuid PRIMARY KEY,
    name     text  NOT NULL,
    settings jsonb NOT NULL DEFAULT '{}'
);

CREATE TABLE user_identity
(
    provider text NOT NULL,
    subject  text NOT NULL,
    user_id  uuid NOT NULL REFERENCES app_user (id),
    PRIMARY KEY (provider, subject)
);

CREATE TABLE game_bundle
(
    id                        uuid PRIMARY KEY,
    name                      text        NOT NULL,
    status                    text        NOT NULL CHECK (status IN ('LOBBY', 'RUNNING', 'FINISHED')),
    turn                      integer     NOT NULL DEFAULT 0,
    host_user_id              uuid        NOT NULL REFERENCES app_user (id),
    winner_team               text,
    waiting_for_player_number integer,
    created_at                timestamptz NOT NULL,
    updated_at                timestamptz NOT NULL,
    format_version            integer     NOT NULL
);

CREATE TABLE participant
(
    game_bundle_id uuid NOT NULL REFERENCES game_bundle (id),
    user_id        uuid NOT NULL REFERENCES app_user (id),
    player_number  integer, -- null for an observer
    PRIMARY KEY (game_bundle_id, user_id),
    UNIQUE (game_bundle_id, player_number)
);

CREATE TABLE journal_entry
(
    game_bundle_id uuid    NOT NULL REFERENCES game_bundle (id),
    sequence       integer NOT NULL, -- the position of the entry in the journal, from 1
    entry          jsonb   NOT NULL,
    PRIMARY KEY (game_bundle_id, sequence)
);
