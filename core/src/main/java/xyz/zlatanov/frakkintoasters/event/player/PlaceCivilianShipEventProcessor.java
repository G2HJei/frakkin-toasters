package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;

public class PlaceCivilianShipEventProcessor extends EventProcessor<PlaceCivilianShipEvent> {

    // "The CAG must, if able, place the civilian ship in a space area that does not already have a civilian ship in
    // it. If all space areas contain a civilian ship, the CAG may place the new civilian ship in any space area."
    // (rule 4.4.3.1)
    @Override
    protected boolean isValid() {
        val location = event.location();
        return location != null
                && GALACTICA_SPACE.locations().contains(location)
                && game.decks().civilianShipsLeft() > 0
                && (hasNoCivilianShip(location) || GALACTICA_SPACE.locations().stream().noneMatch(this::hasNoCivilianShip));
    }

    @Override
    public Followup process() {
        ops.galactica().placeCivilianShip(event.location());
        return Followup.NONE;
    }

    private boolean hasNoCivilianShip(Location location) {
        return galacticaBoard.shipsIn(location, CivilianShip.class).isEmpty();
    }
}
