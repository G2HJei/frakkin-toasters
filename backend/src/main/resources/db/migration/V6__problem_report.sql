-- What a user reports about a game from its table, with where the game stood then (the journal's last sequence).
CREATE TABLE problem_report
(
    id             bigserial PRIMARY KEY,
    game_bundle_id uuid        NOT NULL REFERENCES game_bundle (id),
    user_id        uuid        NOT NULL REFERENCES app_user (id),
    sequence       integer     NOT NULL,
    text           text        NOT NULL,
    created_at     timestamptz NOT NULL
);

CREATE INDEX problem_report_game ON problem_report (game_bundle_id);
