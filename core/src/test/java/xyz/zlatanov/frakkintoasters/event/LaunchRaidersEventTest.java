package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;
import xyz.zlatanov.frakkintoasters.state.ship.CylonShips;
import xyz.zlatanov.frakkintoasters.state.ship.Raider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_HANGAR_BAY;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.STRUCTURAL_DAMAGE;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

class LaunchRaidersEventTest {

    Game           game           = Game.builder().build();
    GalacticaBoard galacticaBoard = game.boards().galactica();

    @Test
    void shouldPlaceBasestarOnCylonFleetBoardWhenNoBasestarsOnMainBoard() {
        val followup = executeEvent();

        assertEquals(
                all(new PlaceShipOnCylonFleetBoardEvent(BASESTAR), new AdvancePursuitTrackEvent()),
                followup);
    }

    @Test
    void shouldLaunchThreeRaidersFromBasestar() {
        val basestar = game.cylonShips().basestar().orElseThrow();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);

        val followup = executeEvent();

        assertEquals(3, galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, Raider.class).size());
        assertEquals(NONE, followup);
    }

    @Test
    void shouldLaunchThreeRaidersFromEachOfMultipleBasestars() {
        val basestar1 = game.cylonShips().basestar().orElseThrow();
        val basestar2 = game.cylonShips().basestar().orElseThrow();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar1);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, basestar2);

        val followup = executeEvent();

        assertEquals(3, galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, Raider.class).size());
        assertEquals(3, galacticaBoard.shipsIn(GALACTICA_SPACE_2_OCLOCK, Raider.class).size());
        assertEquals(NONE, followup);
    }

    @Test
    void shouldStopLaunchingWhenRaiderSupplyIsExhausted() {
        val game = Game.builder()
                .cylonShips(CylonShips.builder().raiders(2).build())
                .build();
        val galacticaBoard = game.boards().galactica();
        val basestar = game.cylonShips().basestar().orElseThrow();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);

        val followup = new LaunchRaidersEvent().execute(game);

        assertEquals(2, galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, Raider.class).size());
        assertEquals(NONE, followup);
    }

    @Test
    void shouldDoNothingWhenOnlyBasestarHasDisabledHangarBay() {
        val basestar = game.cylonShips().basestar().orElseThrow();
        basestar.damage(DISABLED_HANGAR_BAY);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);

        val followup = executeEvent();

        assertEquals(0, galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, Raider.class).size());
        assertEquals(NONE, followup);
    }

    @Test
    void shouldLaunchFromHealthyBasestarButNotFromHangarDisabledOne() {
        val healthy = game.cylonShips().basestar().orElseThrow();
        val disabled = game.cylonShips().basestar().orElseThrow();
        disabled.damage(DISABLED_HANGAR_BAY);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, healthy);
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, disabled);

        val followup = executeEvent();

        assertEquals(3, galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, Raider.class).size());
        assertEquals(0, galacticaBoard.shipsIn(GALACTICA_SPACE_2_OCLOCK, Raider.class).size());
        assertEquals(NONE, followup);
    }

    @Test
    void shouldLaunchRaidersWhenBasestarHasOtherDamageButHangarIsFunctional() {
        val basestar = game.cylonShips().basestar().orElseThrow();
        basestar.damage(STRUCTURAL_DAMAGE);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);

        val followup = executeEvent();

        assertEquals(3, galacticaBoard.shipsIn(GALACTICA_SPACE_8_OCLOCK, Raider.class).size());
        assertEquals(NONE, followup);
    }

    Followup executeEvent() {
        return new LaunchRaidersEvent().execute(game);
    }
}
