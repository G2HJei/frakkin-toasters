package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;

class DestroyCivilianShipsEventProcessorTest extends EventTestHarness<DestroyCivilianShipsEvent> {

    @Test
    void shouldLetTheCurrentPlayerChooseTheShip() {
        val first = civilianShipAt(GALACTICA_SPACE_2_OCLOCK);
        val second = civilianShipAt(GALACTICA_SPACE_8_OCLOCK);

        execute(new DestroyCivilianShipsEvent(1));

        assertFollowup(one(new DestroyCivilianShipEvent(first.id()), new DestroyCivilianShipEvent(second.id())));
    }

    // One at a time: the second choice depends on the first.
    @Test
    void shouldChooseAgainForTheNextShip() {
        val first = civilianShipAt(GALACTICA_SPACE_2_OCLOCK);
        val second = civilianShipAt(GALACTICA_SPACE_8_OCLOCK);

        execute(new DestroyCivilianShipsEvent(2));

        assertFollowup(all(one(new DestroyCivilianShipEvent(first.id()), new DestroyCivilianShipEvent(second.id())),
                single(new DestroyCivilianShipsEvent(1))));
    }

    @Test
    void shouldDoNothingWithoutCivilianShips() {
        execute(new DestroyCivilianShipsEvent(2));
    }
}
