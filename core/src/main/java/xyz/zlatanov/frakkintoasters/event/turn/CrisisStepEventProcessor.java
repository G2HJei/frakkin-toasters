package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveCrisisCardEvent;

import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.GameStep.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class CrisisStepEventProcessor extends EventProcessor<CrisisStepEvent> {


    @Override
    public Followup process() {
        val current = game.currentPlayer();
        if (game.turn().endedEarly()
                || !current.isHuman()// An Infiltrating Cylon Leader counts as human, so has Crisis step(rule 6.2.3.4).
                // Revealed Cylons and characters in the "Brig" have no Crisis step (rules 8.5, 6.1.3).
                || game.locate(current.character()).equals(Optional.of(BRIG))) {
            return NONE;
        }
        ops.changeStep(CRISIS);
        val card = ops.player(current.number()).drawCrisisCard();
        // The card stays with the turn for its icons, until EndTurnEvent discards it (rule 8.8).
        ops.playCrisisCard(card);
        val resolve = single(new ResolveCrisisCardEvent(card));
        // A card that returned to this step has an Activate Cylon Ships step of its own, which the turn doesn't list.
        return event.again() ? all(resolve, single(new ActivateCylonShipsStepEvent())) : resolve;
    }
}
