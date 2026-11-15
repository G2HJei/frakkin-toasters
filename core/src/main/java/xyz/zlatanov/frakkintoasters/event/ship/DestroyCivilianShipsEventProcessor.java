package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DestroyCivilianShipsEventProcessor extends EventProcessor<DestroyCivilianShipsEvent> {

    @Override
    public Followup process() {
        val civilianShips = galacticaBoard.shipsInSpace(CivilianShip.class);
        if (event.count() <= 0 || civilianShips.isEmpty()) {
            return Followup.NONE;
        }
        // One at a time, because the ships left to choose from depend on the first choice.
        val choice = one(civilianShips.stream()
                .map(ship -> new DestroyCivilianShipEvent(ship.id()))
                .toArray(Event[]::new));
        return event.count() > 1 ? all(choice, single(new DestroyCivilianShipsEvent(event.count() - 1))) : choice;
    }
}
