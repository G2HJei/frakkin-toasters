package xyz.zlatanov.frakkintoasters.state.exception;

public class EventConstraintViolationException extends RuntimeException {
    public EventConstraintViolationException(String message) {
        super(message);
    }
}
