package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.Actions;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.GameStep.MOVEMENT;

public class MovementStepEventProcessor extends EventProcessor<MovementStepEvent> {

    // The current player moves, uses a "Movement:" ability instead, or does nothing (rule 8.3).
    @Override
    public Followup process() {
        if (game.turn().endedEarly()) {
            return NONE;
        }
        ops.changeStep(MOVEMENT);
        return Actions.offerMovement(game, game.currentPlayer().number());
    }
}
