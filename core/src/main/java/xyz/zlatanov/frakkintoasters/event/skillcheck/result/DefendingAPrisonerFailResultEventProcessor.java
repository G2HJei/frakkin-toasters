package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DefendingAPrisonerFailResultEventProcessor extends EventProcessor<DefendingAPrisonerFailResultEvent> {

    @Override
    public Followup process() {
        return game.turn().dieRoll().result() <= 4
                ? single(new ExecuteEvent(game.turn().currentPlayerNumber()))
                : NONE;
    }
}
