package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.WEAPONS_CONTROL;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.FOOD;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.FUEL;

class DamageGalacticaEventProcessorTest extends EventTestHarness<DamageGalacticaEvent> {

    @Test
    void shouldDamageGalactica() {
        galacticaDamage.nextCard(GalacticaDamage.WEAPONS_CONTROL);

        execute(new DamageGalacticaEvent());

        assertTrue(galacticaBoard.damagedLocations().contains(WEAPONS_CONTROL));
        assertEquals(7, galacticaDamage.size());
    }

    @Test
    void shouldDamageGalacticaFood() {
        galacticaDamage.nextCard(FOOD);

        execute(new DamageGalacticaEvent());

        assertEquals(7, galacticaBoard.food());
        assertEquals(7, galacticaDamage.size());
    }

    @Test
    void shouldDamageGalacticaFuel() {
        galacticaDamage.nextCard(FUEL);

        execute(new DamageGalacticaEvent());

        assertEquals(7, galacticaBoard.fuel());
        assertEquals(7, galacticaDamage.size());
    }

}