package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DamagePegasusEventProcessorTest extends EventTestHarness<DamagePegasusEvent> {
    @Test
    void shouldDamagePegasus() {
        pegasusDamage.nextCard(PegasusDamage.PEGASUS_CIC);

        executeAndAssertNoFollowup(new DamagePegasusEvent());

        assertTrue(pegasusBoard.damagedLocations().contains(Location.PEGASUS_CIC));
        assertEquals(3, pegasusDamage.size());
    }
}