package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.DamagePegasusEvent;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.*;

class PegasusCicActionEventTest extends EventTest {

    Basestar basestar;

    @BeforeEach
    void setUp() {
        basestar = basestar();
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, basestar);
    }

    @Test
    void shouldDamagePegasus() {
        die.nextRoll(3);
        val followup = executeEvent();
        assertEquals(single(new DamagePegasusEvent()), followup);
    }

    @Test
    void shouldDamageBasestar() {
        basestarDamageDeck.nextCard(DISABLED_WEAPONS);
        die.nextRoll(5);

        executeEvent();

        assertEquals(1, basestar.damage().size());
        assertTrue(basestar.damage().contains(DISABLED_WEAPONS));
        assertEquals(3, basestarDamageDeck.cards().size());

    }

    @Test
    void shouldDamageBasestarTwice() {
        basestarDamageDeck.nextCard(DISABLED_WEAPONS).nextCard(STRUCTURAL_DAMAGE);
        die.nextRoll(8);

        executeEvent();

        assertEquals(2, basestar.damage().size());
        assertEquals(2, basestarDamageDeck.cards().size());

    }

    @Test
    void shouldDamageOnlyOnceIfDestroyed() {
        basestarDamageDeck.nextCard(CRITICAL_HIT);
        basestar.damage(basestarDamageDeck.draw());
        die.nextRoll(8);

        executeEvent();

        assertTrue(game.boards().galactica().shipsIn(GALACTICA_SPACE_2_OCLOCK).isEmpty());
        assertTrue(basestar.damage().isEmpty());
        assertEquals(4, basestarDamageDeck.cards().size());
    }

    Followup executeEvent() {
        return execute(new PegasusCicActionEvent(basestar.id()));
    }
}
