package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

public class DestroyCivilianShipEventProcessor extends EventProcessor<DestroyCivilianShipEvent> {

    @Override
    protected boolean isValid() {
        return galacticaBoard.shipsInSpace(CivilianShip.class).stream().anyMatch(s -> s.id() == event.shipId());
    }

    @Override
    public Followup process() {
        ops.ship(event.shipId()).destroy();
        return Followup.NONE;
    }
}
