package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PresidentsOfficeEventProcessor extends EventProcessor<PresidentsOfficeEvent> {
    @Override
    public Followup process() {
        val drawnCards = game.decks().politics().draw(2);
        player().skillCards().addOnTop(drawnCards);
        return Followup.NONE;
    }

}
