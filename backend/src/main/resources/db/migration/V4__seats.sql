-- Why a finished game ended: an ending of the game (WON), abandoned by a vote or the host, or a lobby the host cancelled.
-- A rematch is the lobby the host opened after the game.
-- The player count is the number of seats once the game has started.
ALTER TABLE game_bundle
    ADD COLUMN end_reason   text CHECK (end_reason IN ('WON', 'ABANDONED', 'CANCELLED')),
    ADD COLUMN rematch_id   uuid REFERENCES game_bundle (id),
    ADD COLUMN player_count integer;

UPDATE game_bundle SET end_reason = 'WON' WHERE status = 'FINISHED';
UPDATE game_bundle g
SET player_count = (SELECT count(*) FROM participant p WHERE p.game_bundle_id = g.id AND p.player_number IS NOT NULL)
WHERE status <> 'LOBBY';

-- Who held each seat of a running game, and why he left it: the results name every holder, and a user who held a seat
-- may only take that one back. The current holder's row has no until.
CREATE TABLE seat_holder
(
    game_bundle_id uuid        NOT NULL REFERENCES game_bundle (id),
    player_number  integer     NOT NULL,
    user_id        uuid        NOT NULL REFERENCES app_user (id),
    since          timestamptz NOT NULL,
    until          timestamptz,
    reason         text CHECK (reason IN ('LEFT', 'VOTED_OUT', 'KICKED', 'BANNED')),
    PRIMARY KEY (game_bundle_id, player_number, since)
);

-- The users the host banned from a game: they can't join it again.
CREATE TABLE game_ban
(
    game_bundle_id uuid NOT NULL REFERENCES game_bundle (id),
    user_id        uuid NOT NULL REFERENCES app_user (id),
    PRIMARY KEY (game_bundle_id, user_id)
);

-- The open votes of a running game, one row per voter: to vacate a seat (player_number), or to abandon the game
-- (player_number 0).
CREATE TABLE vote
(
    game_bundle_id uuid        NOT NULL REFERENCES game_bundle (id),
    kind           text        NOT NULL CHECK (kind IN ('VACATE', 'ABANDON')),
    player_number  integer     NOT NULL,
    user_id        uuid        NOT NULL REFERENCES app_user (id),
    created_at     timestamptz NOT NULL,
    PRIMARY KEY (game_bundle_id, kind, player_number, user_id)
);
