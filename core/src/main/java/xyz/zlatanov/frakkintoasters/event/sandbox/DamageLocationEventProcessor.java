package xyz.zlatanov.frakkintoasters.event.sandbox;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;

public class DamageLocationEventProcessor extends EventProcessor<DamageLocationEvent> {

    @Override
    protected boolean isValid() {
        val location = event.location();
        if (location == null) {
            return false;
        }
        val onGalactica = LocationsArea.GALACTICA.locations().contains(location) && !galacticaBoard.isDamaged(location);
        val onPegasus = LocationsArea.PEGASUS.locations().contains(location) && !pegasusBoard.isDamaged(location);
        return onGalactica || onPegasus;
    }

    @Override
    public Followup process() {
        ops.sandbox().damage(event.location());
        return Followup.NONE;
    }
}
