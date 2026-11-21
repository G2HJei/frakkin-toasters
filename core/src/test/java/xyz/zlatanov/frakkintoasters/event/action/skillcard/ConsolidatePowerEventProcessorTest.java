package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsDiscarded;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.SkillCardPlayed;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsLost;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2_OF_ANY_COLOR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BUILD_NUKE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

class ConsolidatePowerEventProcessorTest extends EventTestHarness<ConsolidatePowerEvent> {

    private static final SkillCard CONSOLIDATE_POWER_1 = new SkillCard(1, CONSOLIDATE_POWER);
    private static final SkillCard CONSOLIDATE_POWER_2 = new SkillCard(2, CONSOLIDATE_POWER);

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(CONSOLIDATE_POWER_1);
    }

    @Test
    void thePlayerDrawsTwoCardsOfAnyColor() {
        execute(new ConsolidatePowerEvent(1, CONSOLIDATE_POWER_1));

        assertFollowup(new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_2_OF_ANY_COLOR));
        assertNoSkillCards(1);
        assertEquals(List.of(CONSOLIDATE_POWER_1), politicsDeck.discardedCards());
        assertEquals(List.of(CONSOLIDATE_POWER), game.turn().playedSkillCards());
    }

    // The card is discarded before its ability resolves.
    @Test
    void playingTheCardIsLogged() {
        execute(new ConsolidatePowerEvent(1, CONSOLIDATE_POWER_1));

        assertFollowup(new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_2_OF_ANY_COLOR));
        assertEquals(List.of(
                        new CardsLost(1, PlayerPile.SKILL, List.of(CONSOLIDATE_POWER_1)),
                        new SkillCardPlayed(1, CONSOLIDATE_POWER_1),
                        new CardsDiscarded(POLITICS, List.of(CONSOLIDATE_POWER_1))),
                journal.facts());
    }

    @Test
    void theCardHasToBeInTheHand() {
        assertInvalid(new ConsolidatePowerEvent(1, CONSOLIDATE_POWER_2));
    }

    @Test
    void theCardHasToBeOfItsType() {
        player(1).gainSkillCards(new SkillCard(6, BUILD_NUKE));
        assertInvalid(new ConsolidatePowerEvent(1, new SkillCard(6, BUILD_NUKE)));
    }

    @Test
    void theCardHasToBeNamed() {
        assertInvalid(new ConsolidatePowerEvent(1, null));
    }

    // Revealed Cylons can't use the abilities of Skill Cards (rule 6.1.2.3).
    @Test
    void aRevealedCylonCannotPlayIt() {
        revealCylon();
        assertInvalid(new ConsolidatePowerEvent(1, CONSOLIDATE_POWER_1));
    }
}
