package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class DrawAndDestroyCivilianShipsEventProcessor extends EventProcessor<DrawAndDestroyCivilianShipsEvent> {

    @Override
    public Followup process() {
        if (event.count() <= 0) {
            return Followup.NONE;
        }
        if (game.decks().civilianShipsLeft() == 0) {
            // "If all remaining civilian ships are currently on the board, the current player may choose any civilian
            // ship on the board to destroy." (rule 13.6)
            return single(new DestroyCivilianShipsEvent(event.count()));
        }
        ops.galactica().destroyDrawnCivilianShip();
        return single(new DrawAndDestroyCivilianShipsEvent(event.count() - 1));
    }
}
