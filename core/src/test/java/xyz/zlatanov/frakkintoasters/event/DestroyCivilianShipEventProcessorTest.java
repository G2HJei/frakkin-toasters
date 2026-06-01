package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;

class DestroyCivilianShipEventProcessorTest extends EventTestHarness<DestroyCivilianShipEvent> {

    @Test
    void shouldLoseShipResourcesAndRemoveShip() {
        val civilianShip = new CivilianShip(2000, 2, 3, 4);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, civilianShip);

        executeAndAssertNoFollowup(new DestroyCivilianShipEvent(2000));

        assertEquals(6, galacticaBoard.fuel());
        assertEquals(7, galacticaBoard.morale());
        assertEquals(8, galacticaBoard.population());
        assertTrue(game.removedComponents().contains(civilianShip));
    }

}