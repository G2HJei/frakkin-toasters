package xyz.zlatanov.frakkintoasters.projection;

/**
 * Who looks at a game: a player, who sees what is public and what he holds, an observer, who sees what is public, or the
 * user of a sandbox, who sees everything.
 */
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

    /**
     * @return whether the viewer is the player
     */
    default boolean is(int playerNumber) {
        return this instanceof PlayerViewer(int number) && number == playerNumber;
    }
}
