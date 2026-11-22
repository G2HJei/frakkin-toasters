package xyz.zlatanov.frakkintoasters.fuzz;

import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Random;
import java.util.function.Consumer;

/**
 * A decision being answered: the game it is asked in, the randomness that picks the answer, and where the findings go.
 */
record Asked(GameView game, PlayerDecisionEvent<?> decision, Random random, Consumer<Finding> findings) {

    int playerNumber() {
        return decision.playerNumber();
    }

    PlayerView player() {
        return game.player(decision.playerNumber());
    }

    boolean has(EventConstraint constraint) {
        return decision.eventConstraints().contains(constraint);
    }
}
