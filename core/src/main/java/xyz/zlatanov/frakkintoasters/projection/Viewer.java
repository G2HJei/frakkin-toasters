package xyz.zlatanov.frakkintoasters.projection;

public sealed interface Viewer {

    record PlayerViewer(int playerNumber) implements Viewer {
    }

    record Observer() implements Viewer {
    }

    /**
     * The one user of a sandbox, who plays every seat: he sees what everyone sees once the game has ended, plus the draw
     * piles in order, every input in full and every scope and fact as recorded.
     */
    record Sandbox() implements Viewer {
    }

    static Viewer player(int playerNumber) {
        return new PlayerViewer(playerNumber);
    }

    static Viewer observer() {
        return new Observer();
    }

    static Viewer sandbox() {
        return new Sandbox();
    }

    default boolean is(int playerNumber) {
        return this instanceof PlayerViewer(int number) && number == playerNumber;
    }
}
