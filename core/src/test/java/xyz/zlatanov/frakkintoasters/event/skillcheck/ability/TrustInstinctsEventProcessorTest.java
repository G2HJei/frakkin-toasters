package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.skillcheck.DetermineSkillCheckAbilitiesOrderEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class TrustInstinctsEventProcessorTest extends EventTestHarness<TrustInstinctsEvent> {

    SkillCard matchingCard    = new SkillCard(0, AT_ANY_COST);
    SkillCard nonMatchingCard = new SkillCard(0, FULL_THROTTLE);
    SkillCard abilityCard     = new SkillCard(0, DOGFIGHT);

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

    // "Resolve Skill Check Abilities on these cards", like any other card in the check.
    @Test
    void shouldResolveTheAbilitiesOfTheAddedCards() {
        destinyDeck.nextCard(matchingCard, abilityCard);
        execute(new TrustInstinctsEvent());
        assertFollowup(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(abilityCard)));
    }

    @Test
    void shouldNotFollowupWithoutAnAbilityCard() {
        destinyDeck.nextCard(matchingCard, nonMatchingCard);
        execute(new TrustInstinctsEvent());
    }

}