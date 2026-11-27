package xyz.zlatanov.frakkintoasters.backend.game;

public class GameNotAvailableException extends RuntimeException {

    public GameNotAvailableException(String message) {
        super(message);
    }
}
