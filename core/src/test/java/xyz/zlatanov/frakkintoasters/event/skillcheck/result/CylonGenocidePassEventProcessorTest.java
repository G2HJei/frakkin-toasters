package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class CylonGenocidePassEventProcessorTest extends EventTestHarness<CylonGenocidePassEvent> {

    @Test
    void shouldDestroyAllCylonShipsOnMainGameBoard() {
        val raider = raiderAt(GALACTICA_SPACE_2_OCLOCK);
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_4_OCLOCK);
        val basestar = basestarAt(GALACTICA_SPACE_12_OCLOCK);

        execute(new CylonGenocidePassEvent());

        assertNoShips(GALACTICA_SPACE_2_OCLOCK);
        assertNoShips(GALACTICA_SPACE_4_OCLOCK);
        assertNoShips(GALACTICA_SPACE_12_OCLOCK);
        assertTrue(cylonShips.raiders().contains(raider));
        assertTrue(cylonShips.heavyRaiders().contains(heavyRaider));
        assertTrue(cylonShips.basestars().contains(basestar));
    }

    @Test
    void shouldNotDestroyHumanShips() {
        val viper = viperAt(GALACTICA_SPACE_6_OCLOCK);
        val civilianShip = civilianShipAt(GALACTICA_SPACE_6_OCLOCK);
        raiderAt(GALACTICA_SPACE_6_OCLOCK);

        execute(new CylonGenocidePassEvent());

        assertEquals(Set.of(viper, civilianShip), Set.copyOf(galacticaBoard.shipsIn(GALACTICA_SPACE_6_OCLOCK)));
    }

    @Test
    void shouldNotDestroyShipsOnCylonFleetBoardOrCenturions() {
        raiderAt(CYLON_FLEET_SPACE_1);
        val centurion = centurion();
        galacticaBoard.boardGalactica(centurion);

        execute(new CylonGenocidePassEvent());

        assertShipCount(CYLON_FLEET_SPACE_1, 1);
        assertTrue(galacticaBoard.boardingPartyTrack().containsKey(centurion));
    }
}
