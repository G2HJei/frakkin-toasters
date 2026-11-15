package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

class DiscardRandomSkillCardsEventProcessorTest extends EventTestHarness<DiscardRandomSkillCardsEvent> {

    @Test
    void shouldDiscardTheNumberOfCards() {
        draw(5);

        execute(new DiscardRandomSkillCardsEvent(1, 3));

        assertEquals(2, player(1).skillCards().size());
        assertEquals(3, politicsDeck.discardSize());
    }

    // "The current player discards 3 random Skill Cards": all of them, with fewer.
    @Test
    void shouldDiscardAllWhenThereAreFewer() {
        draw(2);

        execute(new DiscardRandomSkillCardsEvent(1, 3));

        assertNoSkillCards(1);
    }

    // Not a choice, so "Bait" and "Dradis Contact" don't make the player draw a Mutiny Card (rule A2.3.8.1).
    @Test
    void shouldNotFollowUp() {
        draw(2);

        execute(new DiscardRandomSkillCardsEvent(1, 2));
    }

    private void draw(int count) {
        ops.player(1).drawSkillCards(Collections.<SkillCardColor>nCopies(count, POLITICS));
    }
}
