package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBasestarEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;

class DerelictBasestarEventProcessorTest extends EventTestHarness<DerelictBasestarEvent> {

    // "Place 2 civilian ships behind Galactica and 1 basestar in front of Galactica. Damage the basestar once."
    @Test
    void theBasestarThatWasPlacedIsDamaged() {
        val civilianShipsBefore = galacticaBoard.shipsIn(GALACTICA_SPACE_2_OCLOCK, CivilianShip.class).size();
        val basestarsBefore = galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, Basestar.class).size();

        execute(new DerelictBasestarEvent());

        assertEquals(civilianShipsBefore + 2, galacticaBoard.shipsIn(GALACTICA_SPACE_2_OCLOCK, CivilianShip.class).size());
        val basestars = galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, Basestar.class);
        assertEquals(basestarsBefore + 1, basestars.size());
        assertFollowup(new DamageBasestarEvent(basestars.getLast().id()));
    }
}
