package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.AT_ANY_COST;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.FULL_THROTTLE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class TrustInstinctsEventProcessorTest extends EventTestHarness<TrustInstinctsEvent> {

    SkillCard matchingCard    = new SkillCard(0, AT_ANY_COST);
    SkillCard nonMatchingCard = new SkillCard(0, FULL_THROTTLE);

    @BeforeEach
    void setUp() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldAddToCorrectPile() {
        destinyDeck.nextCard(matchingCard, nonMatchingCard);
        
        execute(new TrustInstinctsEvent());

        assertTrue(game.activeSkillCheck().matchingPile().cards().contains(matchingCard));
        assertTrue(game.activeSkillCheck().nonMatchingPile().cards().contains(nonMatchingCard));
    }

}