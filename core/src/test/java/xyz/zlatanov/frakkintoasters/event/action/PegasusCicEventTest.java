package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.DamagePegasusEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.*;

class PegasusCicEventProcessorTest extends EventTestHarness<PegasusCicEvent> {

    Basestar        basestar;
    PegasusCicEvent event;

    @BeforeEach
    void setUp() {
        basestar = basestarAt(GALACTICA_SPACE_2_OCLOCK);
        event = new PegasusCicEvent(basestar.id());
    }

    @Test
    void shouldDamagePegasus() {
        nextRoll(3);
        execute(event);
        assertFollowup(single(new DamagePegasusEvent()));
    }

    @Test
    void shouldDamageBasestar() {
        basestarDamageDeck.nextCard(DISABLED_WEAPONS);
        nextRoll(5);

        execute(event);

        assertEquals(1, basestar.damage().size());
        assertTrue(basestar.damage().contains(DISABLED_WEAPONS));
        assertEquals(3, basestarDamageDeck.cards().size());

    }

    @Test
    void shouldDamageBasestarTwice() {
        basestarDamageDeck.nextCard(DISABLED_WEAPONS, STRUCTURAL_DAMAGE);
        nextRoll(8);

        execute(event);

        assertEquals(2, basestar.damage().size());
        assertEquals(2, basestarDamageDeck.cards().size());

    }

    @Test
    void shouldDamageOnlyOnceIfDestroyed() {
        basestarDamageDeck.nextCard(CRITICAL_HIT);
        basestar.damage(basestarDamageDeck.draw());
        nextRoll(8);

        execute(event);

        assertNoShips(GALACTICA_SPACE_2_OCLOCK);
        assertTrue(basestar.damage().isEmpty());
        assertEquals(4, basestarDamageDeck.cards().size());
    }

}
