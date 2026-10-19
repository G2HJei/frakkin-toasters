package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

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
        execute(new QuickThinkingEvent(null));
        assertTrue(game.activeSkillCheck().cards().isEmpty());
    }

    @Test
    void shouldGiveSelectedCardToPlayer() {
        game.activeSkillCheck().matchingPile().addOnTop(validCard);

        execute(new QuickThinkingEvent(validCard));

        assertTrue(game.activeSkillCheck().cards().isEmpty());
        assertTrue(player(1).skillCards().cards().contains(validCard));
    }

    @Test
    void shouldNotAllowValueGreaterThan3() {
        game.activeSkillCheck().matchingPile().addOnTop(invalidCard);
        assertInvalid(new QuickThinkingEvent(invalidCard));
    }

    @Test
    void shouldNotAllowDiscardingCardWithBoostedValue() {
        val repair2 = new SkillCard(2, REPAIR);
        game.activeSkillCheck().resolved(ESTABLISH_NETWORK);
        game.activeSkillCheck().matchingPile().addOnTop(repair2);

        assertInvalid(new QuickThinkingEvent(repair2));
    }
}