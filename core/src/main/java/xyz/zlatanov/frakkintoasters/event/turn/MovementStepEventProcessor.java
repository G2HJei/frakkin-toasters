package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.GameStep.MOVEMENT;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class MovementStepEventProcessor extends EventProcessor<MovementStepEvent> {

    @Override
    public Followup process() {
        ops.changeStep(MOVEMENT);
        val current = game.currentPlayer();
        val location = game.locate(current.character());
        if (location.isEmpty() || location.equals(Optional.of(BRIG))) {
            // A character off the board (a "Stranded" Karl "Helo" Agathon) can't move, nor can one in the "Brig" (rule 9.2).
            return NONE;
        }
        return one(new PlayerDecisionEvent<>(current.number(), MoveEvent.class), new NoOpEvent(current.number()));
    }
}
