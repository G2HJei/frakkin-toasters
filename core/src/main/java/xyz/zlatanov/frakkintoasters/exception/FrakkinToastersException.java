package xyz.zlatanov.frakkintoasters.exception;

public abstract class FrakkinToastersException extends RuntimeException {
    public FrakkinToastersException(String message) {
        super(message);
    }
}
