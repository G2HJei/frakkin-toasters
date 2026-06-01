package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PlaceDestinationCardOnTopEventProcessor extends EventProcessor<PlaceDestinationCardOnTopEvent> {

    @Override
    public Followup process() {
        game.decks().destination().addOnTop(event.card());
        return Followup.NONE;
    }
}
