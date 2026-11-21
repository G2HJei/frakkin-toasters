package xyz.zlatanov.frakkintoasters.event.sandbox;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyPegasusEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyPegasusEventProcessor;
import xyz.zlatanov.frakkintoasters.state.board.LocationsArea;

public class DamageLocationEventProcessor extends EventProcessor<DamageLocationEvent> {

    @Override
    protected boolean isValid() {
        val location = event.location();
        if (location == null) {
            return false;
        }
        val onGalactica = LocationsArea.GALACTICA.locations().contains(location) && !galacticaBoard.isDamaged(location);
        val onPegasus = LocationsArea.PEGASUS.locations().contains(location) && !pegasusBoard.isDamaged(location)
                && !pegasusBoard.destroyed();
        return onGalactica || onPegasus;
    }

    @Override
    public Followup process() {
        ops.sandbox().damage(event.location());
        // The fourth Pegasus location destroys Pegasus, as a drawn token would.
        return DestroyPegasusEventProcessor.isWrecked(game) ? Followup.single(new DestroyPegasusEvent()) : Followup.NONE;
    }
}
