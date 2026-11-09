package xyz.zlatanov.frakkintoasters.state.view;

import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import java.util.List;

public interface TurnView {

    /**
     * Counts player turns. It is 0 during setup.
     */
    int number();

    GameStep step();

    int currentPlayerNumber();

    /**
     * The player who is taking an action or moving right now. It is the current player, except when someone else acts
     * during that player's turn (e.g. the target of an "Executive Order" card). It does not change who the "current
     * player" is for game effects, e.g. skill checks.
     */
    int actingPlayerNumber();

    /**
     * The Crisis Cards resolved this turn. They stay with the turn until it ends, when they are discarded (rule 8.8).
     */
    List<CrisisCard> crisisCards();
}
