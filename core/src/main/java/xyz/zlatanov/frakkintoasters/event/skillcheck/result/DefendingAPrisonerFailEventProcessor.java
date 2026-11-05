package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DefendingAPrisonerFailEventProcessor extends EventProcessor<DefendingAPrisonerFailEvent> {

    @Override
    public Followup process() {
        return ops.rollDie() <= 4
                ? single(new ExecuteEvent(game.currentPlayerNumber()))
                : NONE;
    }
}
