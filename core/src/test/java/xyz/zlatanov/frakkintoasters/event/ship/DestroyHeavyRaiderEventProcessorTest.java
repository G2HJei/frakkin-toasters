package xyz.zlatanov.frakkintoasters.event.ship;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.HeavyRaider;

import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_3_OCLOCK;

class DestroyHeavyRaiderEventProcessorTest extends EventTestHarness<DestroyHeavyRaiderEvent> {

    @Test
    void theHeavyRaiderIsDestroyed() {
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_3_OCLOCK);

        execute(new DestroyHeavyRaiderEvent(heavyRaider.id()));

        assertShipCount(GALACTICA_SPACE_3_OCLOCK, HeavyRaider.class, 0);
    }

    @Test
    void notARaider() {
        val raider = raiderAt(GALACTICA_SPACE_3_OCLOCK);

        assertInvalid(new DestroyHeavyRaiderEvent(raider.id()));
    }
}
