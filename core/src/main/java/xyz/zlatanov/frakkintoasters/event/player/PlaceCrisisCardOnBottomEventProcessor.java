package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PlaceCrisisCardOnBottomEventProcessor extends EventProcessor<PlaceCrisisCardOnBottomEvent> {

    @Override
    public Followup process() {
        game.decks().crisis().addToBottom(event.card());
        return Followup.NONE;
    }
}
