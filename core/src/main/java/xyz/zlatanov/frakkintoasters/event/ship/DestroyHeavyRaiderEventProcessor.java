package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;

public class DestroyHeavyRaiderEventProcessor extends EventProcessor<DestroyHeavyRaiderEvent> {

    @Override
    protected boolean isValid() {
        return galacticaBoard.shipsInSpace(HeavyRaider.class).stream().anyMatch(ship -> ship.id() == event.shipId());
    }

    @Override
    public Followup process() {
        ops.ship(event.shipId()).destroy();
        return Followup.NONE;
    }
}
