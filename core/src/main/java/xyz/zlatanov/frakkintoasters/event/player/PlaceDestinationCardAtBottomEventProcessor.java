package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PlaceDestinationCardAtBottomEventProcessor extends EventProcessor<PlaceDestinationCardAtBottomEvent> {

    @Override
    public Followup process() {
        ops.player(event.playerNumber()).placeDestinationCardOnBottom(event.card());
        return Followup.NONE;
    }
}
