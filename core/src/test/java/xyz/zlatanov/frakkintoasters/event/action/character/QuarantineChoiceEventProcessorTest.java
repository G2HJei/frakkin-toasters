package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHERMAN_DOC_COTTLE;

class QuarantineChoiceEventProcessorTest extends EventTestHarness<QuarantineChoiceEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(SHERMAN_DOC_COTTLE);
        moveTo(RESEARCH_LAB, SHERMAN_DOC_COTTLE);
    }

    @Test
    void aNewShipTakesThePlaceOfTheChosenOne() {
        val chosen = civilianShipAt(GALACTICA_SPACE_4_OCLOCK);
        val shipsLeft = game.decks().civilianShipsLeft();

        execute(new QuarantineChoiceEvent(1, chosen.id()));

        assertShipCount(GALACTICA_SPACE_4_OCLOCK, CivilianShip.class, 1);
        assertTrue(galacticaBoard.shipsInSpace(CivilianShip.class).stream().noneMatch(ship -> ship.id() == chosen.id()));
        assertEquals(shipsLeft, game.decks().civilianShipsLeft());
    }

    @Test
    void notAShipThatIsntInSpace() {
        assertInvalid(new QuarantineChoiceEvent(1, 999));
    }
}
