package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBasestarEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamagePegasusEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_3_OCLOCK;

class PegasusCicResultEventProcessorTest extends EventTestHarness<PegasusCicResultEvent> {

    Basestar basestar;

    @BeforeEach
    void setUp() {
        basestar = basestarAt(GALACTICA_SPACE_3_OCLOCK);
    }

    @Test
    void shouldDamagePegasusOn3OrLower() {
        rolled(3);
        execute(new PegasusCicResultEvent(basestar.id()));
        assertFollowup(new DamagePegasusEvent());
    }

    @Test
    void shouldDamageTheBasestarOn4To6() {
        rolled(4);
        execute(new PegasusCicResultEvent(basestar.id()));
        assertFollowup(new DamageBasestarEvent(basestar.id()));
    }

    @Test
    void shouldDamageTheBasestarTwiceOn7Or8() {
        rolled(7);
        execute(new PegasusCicResultEvent(basestar.id()));
        assertFollowup(all(new DamageBasestarEvent(basestar.id()), new DamageBasestarEvent(basestar.id())));
    }
}
