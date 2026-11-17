package xyz.zlatanov.frakkintoasters.backend.game;

/**
 * The game can't be opened: it doesn't exist, hasn't started, or another version of the codec wrote its journal.
 */
public class GameNotAvailableException extends RuntimeException {

    public GameNotAvailableException(String message) {
        super(message);
    }
}
