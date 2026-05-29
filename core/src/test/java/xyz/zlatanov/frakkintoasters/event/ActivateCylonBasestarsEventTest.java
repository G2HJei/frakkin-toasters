package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_WEAPONS;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

class ActivateCylonBasestarsEventTest extends EventTest {

    Basestar basestar1;
    Basestar basestar2;

    @BeforeEach
    void setUp() {
        basestar1 = basestar();
        basestar2 = basestar();
    }

    @Test
    void shouldPlaceBasestarOnCylonFleetBoardWhenNoBasestarsOnMainBoard() {
        val followup = executeEvent();
        assertEquals(
                all(new PlaceShipOnCylonFleetBoardEvent(BASESTAR), new AdvancePursuitTrackEvent()),
                followup);
    }

    @Test
    void shouldFollowupWithSingleBasestarActivation() {
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar1);

        val followup = executeEvent();

        assertEquals(single(new ActivateCylonBasestarEvent(basestar1.id())), followup);
    }

    @Test
    void shouldActivateBasestarsOneByOne() {
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, basestar1);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar2);

        val followup = executeEvent();

        assertEquals(all(
                new ActivateCylonBasestarEvent(basestar1.id()),
                new ActivateCylonBasestarEvent(basestar2.id())), followup);
    }

    @Test
    void shouldDelegateToBasestarActivationWhenOnlyBasestarHasDisabledWeapons() {
        basestar1.damage(DISABLED_WEAPONS);
        galacticaBoard.place(GALACTICA_SPACE_8_OCLOCK, basestar1);

        val followup = executeEvent();

        assertEquals(single(new ActivateCylonBasestarEvent(basestar1.id())), followup);
    }

    Followup executeEvent() {
        return execute(new ActivateCylonBasestarsEvent());
    }
}
