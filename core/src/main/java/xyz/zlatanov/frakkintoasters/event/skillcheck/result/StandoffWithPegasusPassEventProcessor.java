package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

public class StandoffWithPegasusPassEventProcessor extends EventProcessor<StandoffWithPegasusPassEvent> {

    // "The current player may move 1 character from the "Brig" to any other location": on Galactica (appendix A2.3.9.3).
    @Override
    public Followup process() {
        if (galacticaBoard.charactersIn(BRIG).isEmpty()) {
            return Followup.NONE; // the decision would have no answer to give
        }
        val current = game.turn().currentPlayerNumber();
        return FollowupUtils.optional(current, new PlayerDecisionEvent<>(current, MoveFromBrigEvent.class));
    }
}
