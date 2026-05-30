package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
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

class LaunchRaidersEventTest extends EventTest {

    Event event = new LaunchRaidersEvent();

    @Test
    void shouldPlaceBasestarOnCylonFleetBoardWhenNoBasestarsOnMainBoard() {
        executeAndAssertFollowup(event, all(new PlaceShipOnCylonFleetBoardEvent(BASESTAR), new AdvancePursuitTrackEvent()));
    }

    @Test
    void shouldLaunchThreeRaidersFromBasestar() {
        basestarAt(GALACTICA_SPACE_8_OCLOCK);

        executeAndAssertNoFollowup(event);

        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 3);
    }

    @Test
    void shouldLaunchThreeRaidersFromEachOfMultipleBasestars() {
        basestarAt(GALACTICA_SPACE_8_OCLOCK);
        basestarAt(GALACTICA_SPACE_2_OCLOCK);

        executeAndAssertNoFollowup(event);

        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 3);
        assertShipCount(GALACTICA_SPACE_2_OCLOCK, Raider.class, 3);
    }

    @Test
    void shouldStopLaunchingWhenRaiderSupplyIsExhausted() {
        setUpGame(Game.builder()
                .cylonShips(CylonShips.builder().raiders(2).build())
                .build());
        basestarAt(GALACTICA_SPACE_8_OCLOCK);

        executeAndAssertNoFollowup(event);

        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 2);
    }

    @Test
    void shouldDoNothingWhenOnlyBasestarHasDisabledHangarBay() {
        val basestar = basestar();
        basestar.damage(DISABLED_HANGAR_BAY);
        place(GALACTICA_SPACE_8_OCLOCK, basestar);

        executeAndAssertNoFollowup(event);

        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 0);
    }

    @Test
    void shouldLaunchFromHealthyBasestarButNotFromHangarDisabledOne() {
        val healthy = basestar();
        val disabled = basestar();
        disabled.damage(DISABLED_HANGAR_BAY);
        place(GALACTICA_SPACE_8_OCLOCK, healthy);
        place(GALACTICA_SPACE_2_OCLOCK, disabled);

        executeAndAssertNoFollowup(event);

        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 3);
        assertShipCount(GALACTICA_SPACE_2_OCLOCK, Raider.class, 0);
    }

    @Test
    void shouldLaunchRaidersWhenBasestarHasOtherDamageButHangarIsFunctional() {
        val basestar = basestar();
        basestar.damage(STRUCTURAL_DAMAGE);
        place(GALACTICA_SPACE_8_OCLOCK, basestar);

        executeAndAssertNoFollowup(event);

        assertShipCount(GALACTICA_SPACE_8_OCLOCK, Raider.class, 3);
    }

}
