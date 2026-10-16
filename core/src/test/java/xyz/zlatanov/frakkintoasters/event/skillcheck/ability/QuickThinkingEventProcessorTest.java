package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckHolder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.AT_ANY_COST;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheck.ADMIRALS_QUARTERS;

class QuickThinkingEventProcessorTest extends EventTestHarness<QuickThinkingEvent> {

    SkillCheckHolder skillCheck;
    SkillCard        validCard   = new SkillCard(3, AT_ANY_COST);
    SkillCard        invalidCard = new SkillCard(4, AT_ANY_COST);

    @BeforeEach
    void setUp() {
        skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldDoNothing() {
        execute(new QuickThinkingEvent(1, null));
        assertTrue(game.activeSkillCheck().cards().isEmpty());
    }

    @Test
    void shouldGiveSelectedCardToPlayer() {
        game.activeSkillCheck().matchingPile().add(validCard);

        execute(new QuickThinkingEvent(1, validCard));

        assertTrue(game.activeSkillCheck().cards().isEmpty());
        assertTrue(player(1).skillCards().cards().contains(validCard));
    }

    @Test
    void shouldNotAllowValueGreaterThan3() {
        game.activeSkillCheck().matchingPile().add(invalidCard);
        assertInvalid(new QuickThinkingEvent(1, invalidCard));
    }
}