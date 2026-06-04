package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShips;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_HANGAR_BAY;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

class LaunchRaidersEventProcessorTest extends EventTestHarness<LaunchRaidersEvent> {

    LaunchRaidersEvent event = new LaunchRaidersEvent();

    @Test
    void shouldPlaceBasestarOnCylonFleetBoardWhenNoBasestarsOnMainBoard() {
        execute(event);
        assertFollowup(all(new PlaceShipOnCylonFleetBoardEvent(BASESTAR), new AdvancePursuitTrackEvent()));
    }

    @Test
    void shouldLaunchThreeRaidersFromBasestar() {
        basestarAt(GALACTICA_SPACE_8_OCLOCK);

        execute(event);

        assertNoFollowup();
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 3);
    }

    @Test
    void shouldLaunchThreeRaidersFromEachOfMultipleBasestars() {
        basestarAt(GALACTICA_SPACE_8_OCLOCK);
        basestarAt(GALACTICA_SPACE_2_OCLOCK);

        execute(event);

        assertNoFollowup();
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 3);
        assertShipCount(GALACTICA_SPACE_2_OCLOCK, Raider.class, 3);
    }

    @Test
    void shouldStopLaunchingWhenRaiderSupplyIsExhausted() {
        setUpGame(Game.builder()
                .cylonShips(CylonShips.builder().raiders(2).build())
                .build());
        basestarAt(GALACTICA_SPACE_8_OCLOCK);

        execute(event);

        assertNoFollowup();
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 2);
    }

    @Test
    void shouldDoNothingWhenOnlyBasestarHasDisabledHangarBay() {
        basestarAt(GALACTICA_SPACE_8_OCLOCK).damage(DISABLED_HANGAR_BAY);
        execute(event);

        assertNoFollowup();
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 0);
    }

    @Test
    void shouldLaunchFromHealthyBasestarButNotFromHangarDisabledOne() {
        basestarAt(GALACTICA_SPACE_8_OCLOCK);
        basestarAt(GALACTICA_SPACE_2_OCLOCK).damage(DISABLED_HANGAR_BAY);

        execute(event);

        assertNoFollowup();
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 3);
        assertShipCount(GALACTICA_SPACE_2_OCLOCK, Raider.class, 0);
    }

    @Test
    void shouldLaunchRaidersWhenBasestarHasOtherDamageButHangarIsFunctional() {
        basestarAt(GALACTICA_SPACE_8_OCLOCK).damage(STRUCTURAL_DAMAGE);
        execute(event);

        assertNoFollowup();
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 3);
    }

}
