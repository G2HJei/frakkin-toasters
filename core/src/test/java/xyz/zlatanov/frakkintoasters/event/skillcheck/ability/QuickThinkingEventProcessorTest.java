package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class QuickThinkingEventProcessorTest extends EventTestHarness<QuickThinkingEvent> {

    SkillCheck skillCheck;
    SkillCard  validCard   = new SkillCard(3, AT_ANY_COST);
    SkillCard  invalidCard = new SkillCard(4, AT_ANY_COST);

    @BeforeEach
    void setUp() {
        skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldDoNothing() {
        skillCheck.matchingPile().addOnTop(validCard);

        execute(new QuickThinkingEvent(null));

        assertEquals(List.of(validCard), skillCheck.dividedCards());
        assertTrue(journal.facts().isEmpty());
    }

    @Test
    void shouldGiveSelectedCardToPlayer() {
        skillCheck.matchingPile().addOnTop(validCard);

        execute(new QuickThinkingEvent(validCard));

        assertTrue(skillCheck.dividedCards().isEmpty());
        assertTrue(player(1).skillCards().cards().contains(validCard));
    }

    @Test
    void shouldTakeACardFromTheNonMatchingPile() {
        val repair1 = new SkillCard(1, REPAIR);
        skillCheck.nonMatchingPile().addOnTop(repair1);

        execute(new QuickThinkingEvent(repair1));

        assertTrue(skillCheck.dividedCards().isEmpty());
        assertTrue(player(1).skillCards().cards().contains(repair1));
    }

    @Test
    void shouldNotAllowValueGreaterThan3() {
        skillCheck.matchingPile().addOnTop(invalidCard);
        assertInvalid(new QuickThinkingEvent(invalidCard));
    }

    @Test
    void shouldNotAllowDiscardingCardWithBoostedValue() {
        val repair2 = new SkillCard(2, REPAIR);
        skillCheck.resolved(ESTABLISH_NETWORK);
        skillCheck.matchingPile().addOnTop(repair2);

        assertInvalid(new QuickThinkingEvent(repair2));
    }

    @Test
    void shouldNotTakeACardThatIsNotInTheCheck() {
        assertInvalid(new QuickThinkingEvent(validCard));
    }

    // "Not a "Quick Thinking" Card", the one being resolved or another copy.
    @Test
    void shouldNotTakeAQuickThinkingCard() {
        val quickThinking = new SkillCard(0, QUICK_THINKING);
        skillCheck.matchingPile().addOnTop(quickThinking);
        skillCheck.matchingPile().addOnTop(quickThinking);

        assertInvalid(new QuickThinkingEvent(quickThinking));
    }

    // Only the cards in the check's piles: the ones played before the check aren't in it.
    @Test
    void shouldNotTakeACardPlayedBeforeTheCheck() {
        skillCheck.playedBefore(1, List.of(validCard));

        assertInvalid(new QuickThinkingEvent(validCard));
    }
}
