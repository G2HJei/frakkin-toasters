package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.ship.Ship;

import static java.util.Comparator.comparingInt;

public class DestroyRaptorEventProcessor extends EventProcessor<DestroyRaptorEvent> {

    // Raptors are identical, so any raptor in the "Reserves" will do. Nothing happens if there are none. Assault raptors
    // count as vipers, not raptors (rule 11.3.2.2).
    @Override
    public Followup process() {
        galacticaBoard.reserves().stream()
                .filter(Raptor.class::isInstance)
                .min(comparingInt(Ship::id))
                .ifPresent(raptor -> ops.ship(raptor.id()).destroy());
        return Followup.NONE;
    }
}
