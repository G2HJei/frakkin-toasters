package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class MoveCylonShipsToFleetBoardEventProcessorTest extends EventTestHarness<MoveCylonShipsToFleetBoardEvent> {

    // "Move any Cylon ships in space areas on the main game board to the corresponding Cylon space areas on the Cylon
    // Fleet game board."
    @Test
    void cylonShipsMoveToTheCorrespondingSpaceAreas() {
        val raider = raiderAt(GALACTICA_SPACE_3_OCLOCK);
        val basestar = basestarAt(GALACTICA_SPACE_9_OCLOCK);
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_1_OCLOCK);
        val civilian = civilianShipAt(GALACTICA_SPACE_3_OCLOCK);

        execute(new MoveCylonShipsToFleetBoardEvent());

        assertEquals(CYLON_FLEET_SPACE_1, cylonFleetBoard.locate(raider));
        assertEquals(CYLON_FLEET_SPACE_7_8, cylonFleetBoard.locate(basestar));
        assertEquals(CYLON_FLEET_SPACE_3, cylonFleetBoard.locate(heavyRaider));
        // "Do not remove any civilian ships from the main game board."
        assertEquals(GALACTICA_SPACE_3_OCLOCK, galacticaBoard.locate(civilian));
        assertTrue(galacticaBoard.shipsInSpace(Raider.class).isEmpty());
    }

    // "All basestar damage tokens on basestars are removed and randomly mixed into the pile of unused damage tokens."
    @Test
    void basestarDamageGoesBackIntoThePile() {
        val basestar = basestarAt(GALACTICA_SPACE_9_OCLOCK);
        ops.ship(basestar.id()).damage();
        val damage = galacticaBoard.shipsInSpace(Basestar.class).getFirst().damage();
        assertEquals(1, damage.size());
        val pileBefore = game.decks().basestarDamage().size();

        execute(new MoveCylonShipsToFleetBoardEvent());

        assertTrue(cylonFleetBoard.shipsInSpace().keySet().stream().anyMatch(ship -> ship.id() == basestar.id()));
        assertEquals(pileBefore + 1, game.decks().basestarDamage().size());
        assertTrue(basestarDamageDeck.wasShuffled());
        assertInstanceOf(BasestarDamage.class, damage.getFirst());
    }

    // "Any centurion models on the Boarding Party track remain in play when the fleet jumps."
    @Test
    void centurionsStayOnTheBoardingPartyTrack() {
        ops.galactica().placeCenturion();

        execute(new MoveCylonShipsToFleetBoardEvent());

        assertEquals(1, galacticaBoard.boardingPartyTrack().size());
    }
}
