package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PresidentsOfficeEventProcessor extends EventProcessor<PresidentsOfficeEvent> {
    @Override
    public Followup processEvent() {
        val drawnCards = game.decks().politics().draw(2);
        player().skillCards().add(drawnCards);
        return Followup.NONE;
    }

}
