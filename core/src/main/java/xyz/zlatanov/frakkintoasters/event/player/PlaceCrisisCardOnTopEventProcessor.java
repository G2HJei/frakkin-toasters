package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PlaceCrisisCardOnTopEventProcessor extends EventProcessor<PlaceCrisisCardOnTopEvent> {
    @Override
    public Followup process() {
        game.decks().crisis().add(event.card());
        return Followup.NONE;
    }
}
