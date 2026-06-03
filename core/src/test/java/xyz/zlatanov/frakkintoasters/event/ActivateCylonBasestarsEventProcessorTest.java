package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_8_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_WEAPONS;
import static xyz.zlatanov.frakkintoasters.state.ship.ShipType.BASESTAR;

class ActivateCylonBasestarsEventProcessorTest extends EventTestHarness<ActivateCylonBasestarsEvent> {

    Basestar                    basestar1;
    Basestar                    basestar2;
    ActivateCylonBasestarsEvent event = new ActivateCylonBasestarsEvent();

    @BeforeEach
    void setUp() {
        basestar1 = basestar();
        basestar2 = basestar();
    }

    @Test
    void shouldPlaceBasestarOnCylonFleetBoardWhenNoBasestarsOnMainBoard() {
        executeAndAssertFollowup(event, all(new PlaceShipOnCylonFleetBoardEvent(BASESTAR), new AdvancePursuitTrackEvent()));
    }

    @Test
    void shouldFollowupWithSingleBasestarActivation() {
        place(GALACTICA_SPACE_8_OCLOCK, basestar1);

        executeAndAssertFollowup(new ActivateCylonBasestarsEvent(), new ActivateCylonBasestarEvent(basestar1.id()));
    }

    @Test
    void shouldActivateBasestarsOneByOne() {
        place(GALACTICA_SPACE_2_OCLOCK, basestar1);
        place(GALACTICA_SPACE_8_OCLOCK, basestar2);

        executeAndAssertFollowup(new ActivateCylonBasestarsEvent(), all(
                new ActivateCylonBasestarEvent(basestar1.id()),
                new ActivateCylonBasestarEvent(basestar2.id())));
    }

    @Test
    void shouldDelegateToBasestarActivationWhenOnlyBasestarHasDisabledWeapons() {
        basestar1.damage(DISABLED_WEAPONS);
        place(GALACTICA_SPACE_8_OCLOCK, basestar1);

        executeAndAssertFollowup(new ActivateCylonBasestarsEvent(), new ActivateCylonBasestarEvent(basestar1.id()));
    }
}
