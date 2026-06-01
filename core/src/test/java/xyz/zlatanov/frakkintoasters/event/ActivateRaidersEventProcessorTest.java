package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

class ActivateRaidersEventProcessorTest extends EventTestHarness<ActivateRaidersEvent> {

    ActivateRaidersEvent event = new ActivateRaidersEvent();

    @Test
    void shouldPlaceRaiderOnCylonFleetBoardWhenNoRaidersOrBasestarsOnMainBoard() {
        executeAndAssertFollowup(event, all(new PlaceShipOnCylonFleetBoardEvent(RAIDER), new AdvancePursuitTrackEvent()));
    }


    @Test
    void shouldLaunchTwoRaidersFromEachBasestarWhenNoRaidersOnBoard() {
        basestarAt(GALACTICA_SPACE_8_OCLOCK);

        executeAndAssertNoFollowup(event);

        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 2);
    }

    @Test
    void shouldLaunchTwoRaidersFromEachOfMultipleBasestars() {
        basestarAt(GALACTICA_SPACE_8_OCLOCK);
        basestarAt(GALACTICA_SPACE_2_OCLOCK);

        executeAndAssertNoFollowup(event);

        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 2);
        assertShipCount(GALACTICA_SPACE_2_OCLOCK, Raider.class, 2);
    }

    @Test
    void shouldActivateRaidersOneByOne() {
        val raider1 = raider();
        val raider2 = raider();
        place(GALACTICA_SPACE_2_OCLOCK, raider1);
        place(GALACTICA_SPACE_6_OCLOCK, raider2);

        executeAndAssertFollowup(new ActivateRaidersEvent(),
                all(
                        new ActivateRaiderEvent(raider1.id()),
                        new ActivateRaiderEvent(raider2.id())));
    }
}
