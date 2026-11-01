package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ASecondChanceEventProcessor extends EventProcessor<ASecondChanceEvent> {

    @Override
    public Followup process() {
        val currentPlayer = game.currentPlayer();
        if (!currentPlayer.isRevealedCylon()) {
            ops.player(currentPlayer.number()).gainMiracleToken();
        }
        return Followup.NONE;
    }
}
