package xyz.zlatanov.frakkintoasters.backend.journal;

/**
 * JSON that doesn't fit the types, or a value the codec can't write.
 */
public class CodecException extends RuntimeException {

    public CodecException(String message) {
        super(message);
    }

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
