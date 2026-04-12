package xyz.zlatanov.frakkintoasters.state.exception;

public class FrakCallTheAdmiralException extends RuntimeException {
    public FrakCallTheAdmiralException() {
        super("FRAK! Something went wrong... call the admiral (and open a bug)!");
    }

    public FrakCallTheAdmiralException(String message) {
        super(message);
    }
}
