package xyz.zlatanov.frakkintoasters.event.endgame;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;

public class CylonsWinEventProcessor extends EventProcessor<CylonsWinEvent> {

    @Override
    public Followup process() {
        ops.endGame(CYLONS); // the engine runs nothing after it
        return NONE;
    }
}
