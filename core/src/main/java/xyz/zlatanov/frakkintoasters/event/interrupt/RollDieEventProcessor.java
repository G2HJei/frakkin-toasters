package xyz.zlatanov.frakkintoasters.event.interrupt;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class RollDieEventProcessor extends EventProcessor<RollDieEvent> {

    @Override
    protected boolean isValid() {
        return game.turn().dieRoll() != null;
    }

    @Override
    public Followup process() {
        ops.rollDie();
        return Followup.NONE;
    }
}
