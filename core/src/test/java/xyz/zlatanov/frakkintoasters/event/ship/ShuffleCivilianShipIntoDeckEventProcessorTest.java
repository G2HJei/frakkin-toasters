package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_3_OCLOCK;

class ShuffleCivilianShipIntoDeckEventProcessorTest extends EventTestHarness<ShuffleCivilianShipIntoDeckEvent> {

    @Test
    void theShipGoesBackIntoTheDeck() {
        val ship = civilianShipAt(GALACTICA_SPACE_3_OCLOCK);

        execute(new ShuffleCivilianShipIntoDeckEvent(ship.id()));

        assertNoShips(GALACTICA_SPACE_3_OCLOCK);
        assertTrue(civilianShips.cards().stream().anyMatch(card -> card.id() == ship.id()));
    }

    @Test
    void notAShipThatIsNotInSpace() {
        assertInvalid(new ShuffleCivilianShipIntoDeckEvent(99));
    }
}
