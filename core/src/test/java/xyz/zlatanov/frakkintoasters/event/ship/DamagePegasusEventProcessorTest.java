package xyz.zlatanov.frakkintoasters.event.ship;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class DamagePegasusEventProcessorTest extends EventTestHarness<DamagePegasusEvent> {
    @Test
    void shouldDamagePegasus() {
        pegasusDamage.nextCard(PegasusDamage.PEGASUS_CIC);

        execute(new DamagePegasusEvent());

        assertTrue(pegasusBoard.damagedLocations().contains(Location.PEGASUS_CIC));
        assertEquals(3, pegasusDamage.size());
    }

    // The fourth damaged location destroys Pegasus (rule 11.7.2.4).
    @Test
    void shouldDestroyPegasusWhenEveryLocationIsDamaged() {
        pegasusBoard.damage(PEGASUS_CIC).damage(MAIN_BATTERIES).damage(ENGINE_ROOM);
        pegasusDamage.nextCard(PegasusDamage.AIRLOCK);

        execute(new DamagePegasusEvent());

        assertFollowup(new DestroyPegasusEvent());
    }

    @Test
    void shouldRejectADestroyedPegasus() {
        pegasusBoard.destroy();

        assertInvalid(new DamagePegasusEvent());
    }
}
