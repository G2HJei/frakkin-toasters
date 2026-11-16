package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEventProcessor.IN_FRONT;

public class PlaceCivilianShipsInFrontEventProcessor extends EventProcessor<PlaceCivilianShipsInFrontEvent> {

    @Override
    protected boolean isValid() {
        return event.count() >= 0;
    }

    @Override
    public Followup process() {
        // A civilian ship that isn't left isn't placed (rule 13.4).
        val count = Math.min(event.count(), game.decks().civilianShipsLeft());
        for (int i = 0; i < count; i++) {
            ops.galactica().placeCivilianShip(IN_FRONT);
        }
        return Followup.NONE;
    }
}
