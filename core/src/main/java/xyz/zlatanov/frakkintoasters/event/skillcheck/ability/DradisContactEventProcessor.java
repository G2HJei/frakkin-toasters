package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;

public class DradisContactEventProcessor extends EventProcessor<DradisContactEvent> {

    @Override
    public Followup process() {
        ops.galactica().placeRaider(GALACTICA_SPACE_8_OCLOCK);
        ops.galactica().placeRaider(GALACTICA_SPACE_8_OCLOCK);
        return Followup.NONE;
    }
}
