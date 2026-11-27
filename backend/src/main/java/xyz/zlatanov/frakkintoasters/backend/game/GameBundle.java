package xyz.zlatanov.frakkintoasters.backend.game;

import xyz.zlatanov.frakkintoasters.state.Team;

import java.time.Instant;
import java.util.UUID;

/**
 * Only the journal is the game's state; the rest is a summary, written with the entries.
 *
 * @param seed                   a restore needs it to go on like the original
 * @param waitingForPlayerNumber a secret input's too: the lobby only tells a user whether a game waits for him
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
        WON,
        ABANDONED,
        CANCELLED
    }

    public boolean abandoned() {
        return endReason == EndReason.ABANDONED;
    }
}
