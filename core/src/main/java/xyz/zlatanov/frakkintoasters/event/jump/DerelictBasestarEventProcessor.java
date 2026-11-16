package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBasestarEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEventProcessor.BEHIND;
import static xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEventProcessor.IN_FRONT;

public class DerelictBasestarEventProcessor extends EventProcessor<DerelictBasestarEvent> {

    @Override
    public Followup process() {
        // A civilian ship that isn't left isn't placed (rule 13.4).
        val civilianShips = Math.min(2, game.decks().civilianShipsLeft());
        for (int i = 0; i < civilianShips; i++) {
            ops.galactica().placeCivilianShip(BEHIND);
        }
        // No basestar is placed when there is none left in the supply or on the Cylon Fleet board (rule 11.4.2).
        val basestar = ops.galactica().placeBasestar(IN_FRONT);
        return basestar.<Followup>map(id -> single(new DamageBasestarEvent(id))).orElse(NONE);
    }
}
