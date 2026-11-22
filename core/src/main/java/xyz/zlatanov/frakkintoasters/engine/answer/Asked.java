package xyz.zlatanov.frakkintoasters.engine.answer;

import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

/**
 * A decision being answered, and the game it is asked in.
 */
record Asked(GameView game, PlayerDecisionEvent<?> decision) {

    int playerNumber() {
        return decision.playerNumber();
    }

    PlayerView player() {
        return game.player(decision.playerNumber());
    }

    boolean has(EventConstraint constraint) {
        return decision.eventConstraints().contains(constraint);
    }

    boolean isPresident() {
        return player().character() != null && player().character() == game.president();
    }
}
