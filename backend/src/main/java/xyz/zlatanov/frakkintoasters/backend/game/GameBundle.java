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
                         Long seed) {

    public enum GameMode {
        STANDARD,
        SANDBOX
    }

    public enum GameStatus {
        LOBBY,
        RUNNING,
        FINISHED
    }
}
