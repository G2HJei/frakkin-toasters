package xyz.zlatanov.frakkintoasters.event.skillcheck;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ASecondChanceEventProcessor extends EventProcessor<ASecondChanceEvent> {

    @Override
    public Followup process() {
        // "the current player gains 1 miracle token", if he can (rules 4.3.1, 4.3.2)
        ops.player(game.currentPlayer().number()).gainMiracleToken();
        return Followup.NONE;
    }
}
