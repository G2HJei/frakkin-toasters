package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;

class DrawAndDestroyCivilianShipsEventProcessorTest extends EventTestHarness<DrawAndDestroyCivilianShipsEvent> {

    // "Draw a civilian ship and destroy it": the resources on its face are lost, and it leaves the game (rule 11.7.1.3).
    @Test
    void shouldDestroyTheShipThatIsDrawn() {
        val ship = civilianShips.cards().getFirst();
        val population = galacticaBoard.population();

        execute(new DrawAndDestroyCivilianShipsEvent(1));

        assertTrue(game.removedComponents().contains(ship));
        assertEquals(population - ship.populationCost(), galacticaBoard.population());
        assertFollowup(single(new DrawAndDestroyCivilianShipsEvent(0)));
    }

    @Test
    void shouldDrawTheNextShipForTheRest() {
        execute(new DrawAndDestroyCivilianShipsEvent(2));

        assertFollowup(single(new DrawAndDestroyCivilianShipsEvent(1)));
    }

    @Test
    void shouldDoNothingForZeroShips() {
        execute(new DrawAndDestroyCivilianShipsEvent(0));
    }

    // "If all remaining civilian ships are currently on the board, the current player may choose any civilian ship on the
    // board to destroy." (rule 13.6)
    @Test
    void shouldLetTheCurrentPlayerChooseWhenNoneIsLeftToDraw() {
        civilianShipAt(GALACTICA_SPACE_2_OCLOCK);
        while (game.decks().civilianShipsLeft() > 0) {
            civilianShips.draw();
        }

        execute(new DrawAndDestroyCivilianShipsEvent(1));

        assertFollowup(single(new DestroyCivilianShipsEvent(1)));
    }
}
