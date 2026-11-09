package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.GameStep.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class CrisisStepEventProcessor extends EventProcessor<CrisisStepEvent> {


    @Override
    public Followup process() {
        val current = game.currentPlayer();
        if (!current.isHuman()// An Infiltrating Cylon Leader counts as human, so has Crisis step(rule 6.2.3.4).
                // Revealed Cylons and characters in the "Brig" have no Crisis step (rules 8.5, 6.1.3).
                || game.locate(current.character()).equals(Optional.of(BRIG))) {
            return NONE;
        }
        ops.changeStep(CRISIS);
        // The card stays with the turn for its icons, until EndTurnEvent discards it (rule 8.8).
        ops.playCrisisCard(ops.player(current.number()).drawCrisisCard());
        // todo resolve the card
        return NONE;
    }
}
