package xyz.zlatanov.frakkintoasters.backend.game;

import xyz.zlatanov.frakkintoasters.state.Team;

import java.time.Instant;
import java.util.UUID;

/**
 * A game as the lobby knows it. Only the journal is the game's state; the rest is a summary, written with the entries.
 *
 * @param seed                   the seed of a seeded game, which a restore needs to go on like the original
 * @param waitingForPlayerNumber the player the game waits for, a secret input's too: the lobby only tells a user whether
 *                               a game waits for him
 * @param endReason              why a finished game ended, {@code null} before
 * @param rematchId              the lobby the host opened after the game, if any
 * @param playerCount            the seats, once the game has started
 */
public record GameBundle(UUID id,
                         String name,
                         GameStatus status,
                         int turn,
                         UUID hostUserId,
                         Team winnerTeam,
                         Integer waitingForPlayerNumber,
                         Instant createdAt,
                         Instant updatedAt,
                         int formatVersion,
                         GameMode mode,
                         Long seed,
                         EndReason endReason,
                         UUID rematchId,
                         Integer playerCount) {

    public enum GameMode {
        STANDARD,
        SANDBOX
    }

    public enum GameStatus {
        LOBBY,
        RUNNING,
        FINISHED
    }

    public enum EndReason {
        /**
         * The game came to an ending: a team won.
         */
        WON,
        /**
         * The host or a vote of the players ended it without a winner.
         */
        ABANDONED,
        /**
         * The host cancelled the lobby.
         */
        CANCELLED
    }

    public boolean abandoned() {
        return endReason == EndReason.ABANDONED;
    }
}
