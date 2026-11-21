package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.Actions;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.GameStep.MOVEMENT;

public class MovementStepEventProcessor extends EventProcessor<MovementStepEvent> {

    // The current player moves, uses a "Movement:" ability instead, or does nothing (rule 8.3).
    @Override
    public Followup process() {
        if (game.turn().endedEarly()) {
            return NONE;
        }
        ops.changeStep(MOVEMENT);
        val current = game.currentPlayer();
        val movement = Actions.offerMovement(game, current.number());
        return EndOfMovementStepEventProcessor.actsThen(current)
                ? all(movement, single(new EndOfMovementStepEvent()))
                : movement;
    }
}
