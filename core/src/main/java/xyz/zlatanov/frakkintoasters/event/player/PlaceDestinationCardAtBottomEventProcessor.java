package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PlaceDestinationCardAtBottomEventProcessor extends EventProcessor<PlaceDestinationCardAtBottomEvent> {

    @Override
    public Followup process() {
        game.decks().destination().addToBottom(event.card());
        return Followup.NONE;
    }
}
