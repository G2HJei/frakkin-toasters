package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.BaitEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.DradisContactEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.ExploitAWeaknessEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class DrawAndResolveRecklessCardsEventProcessorTest extends EventTestHarness<DrawAndResolveRecklessCardsEvent> {

    SkillCard bait0          = new SkillCard(0, BAIT);
    SkillCard bait1          = new SkillCard(1, BAIT);
    SkillCard dradisContact2 = new SkillCard(2, DRADIS_CONTACT);
    SkillCard exploit0       = new SkillCard(0, EXPLOIT_A_WEAKNESS);
    SkillCard personalVices0 = new SkillCard(0, PERSONAL_VICES);

    @BeforeEach
    void setUp() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldDiscardCardWithStrengthAboveZeroWithoutResolvingIt() {
        treacheryDeck.nextCard(bait1);
        execute(new DrawAndResolveRecklessCardsEvent());
        assertEquals(List.of(bait1), treacheryDeck.discardedCards());
    }

    @Test
    void shouldResolveBothCardsWhenFirstHasZeroStrength() {
        treacheryDeck.nextCard(bait0, dradisContact2);

        execute(new DrawAndResolveRecklessCardsEvent());

        assertEquals(List.of(bait0, dradisContact2), treacheryDeck.discardedCards());
        assertFollowup(all(new BaitEvent(), new DradisContactEvent()));
    }

    @Test
    void shouldDrawOnlyOneMoreCardWhenSecondAlsoHasZeroStrength() {
        treacheryDeck.nextCard(bait0, exploit0, personalVices0);

        execute(new DrawAndResolveRecklessCardsEvent());

        assertEquals(List.of(bait0, exploit0), treacheryDeck.discardedCards());
        assertFollowup(all(
                single(new BaitEvent()),
                single(new PlayerDecisionEvent<>(1, ExploitAWeaknessEvent.class))));
    }

    @Test
    void shouldResolveAbilitiesEvenIfAlreadyResolved() {
        game.activeSkillCheck().resolved(BAIT);
        treacheryDeck.nextCard(bait0, bait1);

        execute(new DrawAndResolveRecklessCardsEvent());

        assertFollowup(all(new BaitEvent(), new BaitEvent()));
    }

    @Test
    void shouldNotAddRevealedCardsToSkillCheck() {
        treacheryDeck.nextCard(bait0, dradisContact2);

        execute(new DrawAndResolveRecklessCardsEvent());

        assertTrue(game.activeSkillCheck().cards().isEmpty());
        assertTrue(game.activeSkillCheck().matchingPile().isEmpty());
        assertTrue(game.activeSkillCheck().nonMatchingPile().isEmpty());
        assertFollowup(all(new BaitEvent(), new DradisContactEvent()));
    }
}
