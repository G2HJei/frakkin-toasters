package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

public class ShuffleCivilianShipIntoDeckEventProcessor extends EventProcessor<ShuffleCivilianShipIntoDeckEvent> {

    @Override
    protected boolean isValid() {
        return galacticaBoard.shipsInSpace(CivilianShip.class).stream().anyMatch(ship -> ship.id() == event.civilianShipId());
    }

    // The same move as an escort's.
    @Override
    public Followup process() {
        ops.ship(event.civilianShipId()).escort();
        return Followup.NONE;
    }
}
