package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.PlaceCenturionsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class CylonPowerPlaceCenturionEventProcessor extends EventProcessor<CylonPowerPlaceCenturionEvent> {

    @Override
    public Followup process() {
        return single(new PlaceCenturionsEvent(1));
    }
}
