package xyz.zlatanov.frakkintoasters.event.interrupt;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.DieRolled;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.STRATEGIC_PLANNING;

class RollDieEventProcessorTest extends EventTestHarness<RollDieEvent> {

    @Test
    void shouldRollTheStartedDieRoll() {
        ops.startDieRoll();
        ops.playOnDieRoll(STRATEGIC_PLANNING, 2);
        nextRoll(5);

        execute(new RollDieEvent());

        assertEquals(5, game.turn().dieRoll().value());
        assertEquals(7, game.turn().dieRoll().result());
    }

    @Test
    void shouldRerollAndKeepTheCards() {
        rolled(5);
        ops.playOnDieRoll(STRATEGIC_PLANNING, 2);
        nextRoll(1);

        execute(new RollDieEvent());

        assertEquals(1, game.turn().dieRoll().value());
        assertEquals(List.of(STRATEGIC_PLANNING), game.turn().dieRoll().cards());
    }

    // "Hotshot" or "Longshot" set the face instead of the die.
    @Test
    void shouldNotRollAFaceAnAbilitySet() {
        ops.startDieRoll();
        ops.replaceDieRoll(6, false);

        execute(new RollDieEvent());

        assertEquals(6, game.turn().dieRoll().value());
        assertTrue(journal.facts().stream().noneMatch(DieRolled.class::isInstance));
    }

    @Test
    void shouldRollAStartedDieRoll() {
        assertInvalid(new RollDieEvent());
    }
}
