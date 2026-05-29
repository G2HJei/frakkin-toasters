package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.board.Location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.*;

class DamageGalacticaEventTest extends EventTest {

    @Test
    void shouldDamageGalactica() {
        galacticaDamage.nextCard(WEAPONS_CONTROL);

        executeEvent();

        assertTrue(galacticaBoard.damagedLocations().contains(Location.WEAPONS_CONTROL));
        assertEquals(7, galacticaDamage.size());
    }

    @Test
    void shouldDamageGalacticaFood() {
        galacticaDamage.nextCard(FOOD);

        executeEvent();

        assertEquals(7, galacticaBoard.food());
        assertEquals(7, galacticaDamage.size());
    }

    @Test
    void shouldDamageGalacticaFuel() {
        galacticaDamage.nextCard(FUEL);

        executeEvent();

        assertEquals(7, galacticaBoard.fuel());
        assertEquals(7, galacticaDamage.size());
    }

    void executeEvent() {
        execute(new DamageGalacticaEvent());
    }
}