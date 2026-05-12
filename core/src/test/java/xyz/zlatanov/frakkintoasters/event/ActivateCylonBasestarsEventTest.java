package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.GalacticaBoard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_WEAPONS;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

class ActivateCylonBasestarsEventTest {

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
    void shouldFollowupWithSingleBasestarActivation() {
        val basestar = game.cylonShips().basestar().orElseThrow();
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);

        val followup = executeEvent();

        assertEquals(single(new ActivateCylonBasestarEvent(basestar.id())), followup);
    }

    @Test
    void shouldActivateBasestarsOneByOne() {
        val basestar1 = game.cylonShips().basestar().orElseThrow();
        val basestar2 = game.cylonShips().basestar().orElseThrow();
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, basestar1);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar2);

        val followup = executeEvent();

        assertEquals(all(
                new ActivateCylonBasestarEvent(basestar1.id()),
                new ActivateCylonBasestarEvent(basestar2.id())), followup);
    }

    @Test
    void shouldDelegateToBasestarActivationWhenOnlyBasestarHasDisabledWeapons() {
        val basestar = game.cylonShips().basestar().orElseThrow();
        basestar.damage(DISABLED_WEAPONS);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar);

        val followup = executeEvent();

        assertEquals(single(new ActivateCylonBasestarEvent(basestar.id())), followup);
    }

    Followup executeEvent() {
        return new ActivateCylonBasestarsEvent().execute(game);
    }
}
