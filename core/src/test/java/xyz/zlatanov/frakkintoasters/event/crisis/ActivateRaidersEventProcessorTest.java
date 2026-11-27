package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeRaidersActivate;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipOnCylonFleetBoardEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_HANGAR_BAY;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.RAIDER;

class ActivateRaidersEventProcessorTest extends EventTestHarness<ActivateRaidersEvent> {

    ActivateRaidersEvent event = new ActivateRaidersEvent();

    @Test
    void shouldPlaceRaiderOnCylonFleetBoardWhenNoRaidersOrBasestarsOnMainBoard() {
        execute(event);
        assertFollowup(Followup.all(new PlaceShipOnCylonFleetBoardEvent(RAIDER), new AdvancePursuitTrackEvent()));
    }


    @Test
    void shouldLaunchTwoRaidersFromEachBasestarWhenNoRaidersOnBoard() {
        basestarAt(GALACTICA_SPACE_9_OCLOCK);
        execute(event);
        assertShipCount(GALACTICA_SPACE_9_OCLOCK, Raider.class, 2);
    }

    // "Disabled Hangar": the basestar may not launch raiders (rule 11.7.3).
    @Test
    void shouldNotLaunchFromABasestarWithADisabledHangar() {
        basestarAt(GALACTICA_SPACE_9_OCLOCK).damage(DISABLED_HANGAR_BAY);
        basestarAt(GALACTICA_SPACE_3_OCLOCK);

        execute(event);

        assertShipCount(GALACTICA_SPACE_9_OCLOCK, Raider.class, 0);
        assertShipCount(GALACTICA_SPACE_3_OCLOCK, Raider.class, 2);
    }

    @Test
    void shouldLaunchTwoRaidersFromEachOfMultipleBasestars() {
        basestarAt(GALACTICA_SPACE_9_OCLOCK);
        basestarAt(GALACTICA_SPACE_3_OCLOCK);

        execute(event);

        assertShipCount(GALACTICA_SPACE_9_OCLOCK, Raider.class, 2);
        assertShipCount(GALACTICA_SPACE_3_OCLOCK, Raider.class, 2);
    }

    @Test
    void shouldActivateRaidersOneByOneAfterRunInterference() {
        val raider1 = raiderAt(GALACTICA_SPACE_3_OCLOCK);
        val raider2 = raiderAt(GALACTICA_SPACE_7_OCLOCK);

        execute(new ActivateRaidersEvent());

        assertFollowup(
                all(new OpenInterruptWindowEvent(new BeforeRaidersActivate()),
                        new ActivateRaiderAreasEvent(List.of(raider1.id(), raider2.id()))));
    }

    // "Negotiation": no Cylon ship launches or activates for the rest of the turn.
    @Test
    void nothingWhileTheShipsAreHeld() {
        basestarAt(GALACTICA_SPACE_9_OCLOCK);
        raiderAt(GALACTICA_SPACE_3_OCLOCK);
        ops.holdCylonShips();

        execute(new ActivateRaidersEvent());

        assertShipCount(GALACTICA_SPACE_9_OCLOCK, Raider.class, 0);
        assertShipCount(GALACTICA_SPACE_3_OCLOCK, Raider.class, 1);
    }
}
