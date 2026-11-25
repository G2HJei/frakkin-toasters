-- The chat of a game, apart from its journal: it is not game state. One chat for everyone at the table, players and
-- observers; the seat is the one the writer held then, none for an observer.
CREATE TABLE chat_message
(
    id             bigserial PRIMARY KEY,
    game_bundle_id uuid        NOT NULL REFERENCES game_bundle (id),
    user_id        uuid        NOT NULL REFERENCES app_user (id),
    player_number  integer,
    text           text        NOT NULL,
    created_at     timestamptz NOT NULL
);

CREATE INDEX chat_message_game ON chat_message (game_bundle_id, id);
