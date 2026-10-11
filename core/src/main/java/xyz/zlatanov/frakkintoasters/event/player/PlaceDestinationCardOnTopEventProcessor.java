package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PlaceDestinationCardOnTopEventProcessor extends EventProcessor<PlaceDestinationCardOnTopEvent> {

    @Override
    public Followup process() {
        ops.player(event.playerNumber()).placeDestinationCardOnTop(event.card());
        return Followup.NONE;
    }
}
