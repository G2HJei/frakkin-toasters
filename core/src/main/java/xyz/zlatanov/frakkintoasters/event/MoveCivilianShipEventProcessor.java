package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class MoveCivilianShipEventProcessor extends EventProcessor<MoveCivilianShipEvent> {

    @Override
    protected boolean isValid() {
        val ship = galacticaBoard.shipsInSpace(CivilianShip.class).stream()
                .filter(s -> s.id() == event.civilianShipId())
                .findFirst();
        return ship.isPresent() && galacticaBoard.locate(ship.get()).adjacentLocations().contains(event.location());
    }

    @Override
    public Followup process() {
        ops.ship(event.civilianShipId()).moveTo(event.location());
        return NONE;
    }
}
