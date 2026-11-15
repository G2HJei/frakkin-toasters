package xyz.zlatanov.frakkintoasters.event.board;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class PlaceCenturionsEventProcessor extends EventProcessor<PlaceCenturionsEvent> {

    // A centurion that isn't left in its supply isn't placed (rule 13.4).
    @Override
    public Followup process() {
        for (int i = 0; i < event.count(); i++) {
            ops.galactica().placeCenturion();
        }
        return Followup.NONE;
    }
}
