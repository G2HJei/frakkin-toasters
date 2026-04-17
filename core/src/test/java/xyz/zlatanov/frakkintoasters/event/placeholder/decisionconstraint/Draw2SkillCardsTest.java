package xyz.zlatanov.frakkintoasters.event.placeholder.decisionconstraint;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.decisionconstraint.Draw2SkillCards;
import xyz.zlatanov.frakkintoasters.event.player.ReceiveSkillCardsEvent;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;

class Draw2SkillCardsTest {
    @Test
    void shouldValidateCorrectEvent() {
        val event = new ReceiveSkillCardsEvent(1, Map.of(TREACHERY, 2));
        val constraint = new Draw2SkillCards();
        assertTrue(constraint.validConstraint(event));
    }

    @Test
    void shouldNotValidateIncorrectEvent() {
        val event = new ReceiveSkillCardsEvent(1, Map.of());
        val constraint = new Draw2SkillCards();
        assertFalse(constraint.validConstraint(event));
    }
}