package xyz.zlatanov.frakkintoasters.event.ship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.interrupt.OpenInterruptWindowEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterBasestarOrCenturionDestroyed;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_3_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.*;

class DamageBasestarEventProcessorTest extends EventTestHarness<DamageBasestarEvent> {

    Basestar basestar;

    @BeforeEach
    void setUp() {
        basestar = basestarAt(GALACTICA_SPACE_3_OCLOCK);
    }

    @Test
    void shouldDrawADamageToken() {
        basestarDamageDeck.nextCard(DISABLED_WEAPONS);

        execute(new DamageBasestarEvent(basestar.id()));

        assertEquals(List.of(DISABLED_WEAPONS), basestar.damage());
        assertEquals(3, basestarDamageDeck.cards().size());
    }

    // "Major Victory": "Play after you destroy a basestar or a centurion on the Boarding Party track."
    @Test
    void shouldDestroyTheBasestarAt3Damage() {
        basestarDamageDeck.nextCard(STRUCTURAL_DAMAGE, DISABLED_HANGAR_BAY, DISABLED_WEAPONS);
        basestar.damage(basestarDamageDeck.draw()).damage(basestarDamageDeck.draw());

        execute(new DamageBasestarEvent(basestar.id()));

        assertDestroyed();
    }

    @Test
    void shouldCountACriticalHitAsTwo() {
        basestarDamageDeck.nextCard(CRITICAL_HIT, DISABLED_WEAPONS);
        basestar.damage(basestarDamageDeck.draw());

        execute(new DamageBasestarEvent(basestar.id()));

        assertDestroyed();
    }

    @Test
    void shouldDoNothingOnceTheBasestarIsDestroyed() {
        execute(new DamageBasestarEvent(basestar().id()));

        assertEquals(4, basestarDamageDeck.cards().size());
    }

    private void assertDestroyed() {
        assertFollowup(new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed()));
        assertTrue(basestar.damage().isEmpty());
        assertEquals(4, basestarDamageDeck.cards().size()); // the tokens go back into the pile
        assertTrue(galacticaBoard.shipsIn(GALACTICA_SPACE_3_OCLOCK).isEmpty());
    }
}
