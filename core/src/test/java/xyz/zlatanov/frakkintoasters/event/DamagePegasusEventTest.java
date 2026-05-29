package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage.PEGASUS_CIC;

class DamagePegasusEventTest extends EventTest {
    @Test
    void shouldDamagePegasus() {
        pegasusDamage.nextCard(PEGASUS_CIC);

        execute(new DamagePegasusEvent());

        assertTrue(pegasusBoard.damagedLocations().contains(Location.PEGASUS_CIC));
        assertEquals(3, pegasusDamage.size());
    }
}