package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PlaceRaiderEventProcessor extends EventProcessor<PlaceRaiderEvent> {

    @Override
    public Followup process() {
        ops.galactica().placeRaider(event.location());
        return Followup.NONE;
    }
}
