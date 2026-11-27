package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_3_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PEGASUS_CIC;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;

class PegasusCicEventProcessorTest extends EventTestHarness<PegasusCicEvent> {

    Basestar basestar;

    @BeforeEach
    void setUp() {
        player(1).character(HELENA_CAIN);
        moveTo(PEGASUS_CIC, HELENA_CAIN);
        basestar = basestarAt(GALACTICA_SPACE_3_OCLOCK);
    }

    @Test
    void shouldRollForTheChosenBasestar() {
        execute(new PegasusCicEvent(1, basestar.id()));

        assertFollowup(rollDie(new PegasusCicResultEvent(basestar.id())));
    }

    @Test
    void shouldChooseABasestarInSpace() {
        assertInvalid(new PegasusCicEvent(1, basestar().id()));
    }
}
