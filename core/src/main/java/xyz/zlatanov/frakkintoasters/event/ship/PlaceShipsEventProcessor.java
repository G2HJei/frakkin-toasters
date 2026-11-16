package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;

public class PlaceShipsEventProcessor extends EventProcessor<PlaceShipsEvent> {

    // In front of Galactica is where the Cylons start the game and where "Dradis Contact" places raiders, behind it is
    // where the civilian ships start and "Bait" places one.
    public static final Location IN_FRONT = GALACTICA_SPACE_8_OCLOCK;
    public static final Location BEHIND   = GALACTICA_SPACE_2_OCLOCK;

    @Override
    public Followup process() {
        // The ops take a Cylon ship from the Cylon Fleet board when the supply has none left (rule 11.4.2).
        for (int i = 0; i < event.basestars(); i++) {
            ops.galactica().placeBasestar(IN_FRONT);
        }
        for (int i = 0; i < event.raiders(); i++) {
            ops.galactica().placeRaider(IN_FRONT);
        }
        // A civilian ship that isn't left isn't placed (rule 13.4).
        int civilianShips = Math.min(event.civilianShips(), game.decks().civilianShipsLeft());
        for (int i = 0; i < civilianShips; i++) {
            ops.galactica().placeCivilianShip(BEHIND);
        }
        return Followup.NONE;
    }
}
