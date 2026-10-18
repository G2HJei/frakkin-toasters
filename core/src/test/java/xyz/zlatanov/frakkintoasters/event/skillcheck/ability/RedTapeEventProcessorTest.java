package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class RedTapeEventProcessorTest extends EventTestHarness<RedTapeEvent> {

    @Test
    void shouldRemove5And6ValueSkillCards() {
        val card0 = new SkillCard(0, AT_ANY_COST);
        val card1 = new SkillCard(1, BAIT);
        val card2 = new SkillCard(2, AT_ANY_COST);
        val card3 = new SkillCard(3, BAIT);
        val card4 = new SkillCard(4, AT_ANY_COST);
        val card5 = new SkillCard(5, BAIT);
        val card6 = new SkillCard(6, AT_ANY_COST);
        game.startSkillCheck(ADMIRALS_QUARTERS);
        game.activeSkillCheck().matchingPile().addOnTop(List.of(card0, card2, card4, card6));
        game.activeSkillCheck().nonMatchingPile().addOnTop(List.of(card1, card3, card5));

        execute(new RedTapeEvent());

        assertEquals(List.of(card0, card2, card4), game.activeSkillCheck().matchingPile().cards());
        assertEquals(List.of(card1, card3), game.activeSkillCheck().nonMatchingPile().cards());
    }

    @Test
    void shouldRemoveBoostedEngineeringCard() {
        val engineeringTwoValueCard = new SkillCard(3, REPAIR);
        game.startSkillCheck(ADMIRALS_QUARTERS);
        game.activeSkillCheck().nonMatchingPile().addOnTop(engineeringTwoValueCard);
        game.activeSkillCheck().resolved(ESTABLISH_NETWORK);

        execute(new RedTapeEvent());

        assertEquals(List.of(), game.activeSkillCheck().nonMatchingPile().cards());
    }

}