package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;

public class BaitEventProcessor extends EventProcessor<BaitEvent> {

    @Override
    public Followup process() {
        ops.galactica().placeCivilianShip(GALACTICA_SPACE_2_OCLOCK);
        return Followup.NONE;
    }
}
