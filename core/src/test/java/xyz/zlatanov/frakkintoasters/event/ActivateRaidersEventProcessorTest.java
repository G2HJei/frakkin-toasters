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
        execute(event);
        assertFollowup(all(new PlaceShipOnCylonFleetBoardEvent(RAIDER), new AdvancePursuitTrackEvent()));
    }


    @Test
    void shouldLaunchTwoRaidersFromEachBasestarWhenNoRaidersOnBoard() {
        basestarAt(GALACTICA_SPACE_8_OCLOCK);
        execute(event);
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 2);
    }

    @Test
    void shouldLaunchTwoRaidersFromEachOfMultipleBasestars() {
        basestarAt(GALACTICA_SPACE_8_OCLOCK);
        basestarAt(GALACTICA_SPACE_2_OCLOCK);

        execute(event);

        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 2);
        assertShipCount(GALACTICA_SPACE_2_OCLOCK, Raider.class, 2);
    }

    @Test
    void shouldActivateRaidersOneByOne() {
        val raider1 = raiderAt(GALACTICA_SPACE_2_OCLOCK);
        val raider2 = raiderAt(GALACTICA_SPACE_6_OCLOCK);

        execute(new ActivateRaidersEvent());

        assertFollowup(
                all(
                        new ActivateRaiderEvent(raider1.id()),
                        new ActivateRaiderEvent(raider2.id())));
    }
}
