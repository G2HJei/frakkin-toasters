package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ResurrectionShipEventProcessor extends EventProcessor<ResurrectionShipEvent> {
    @Override
    public Followup process() {
        val drawn = game.decks().superCrisis().draw();
        player.superCrisisCards().addOnTop(drawn);
        return Followup.NONE;
    }
}
