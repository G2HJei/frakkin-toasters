package xyz.zlatanov.frakkintoasters.fuzz;

import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON_SELFISH;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealPersonalGoalEvent;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.IMPEACHMENT;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.ImpeachmentEvent;
import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.action.quorum.AuthorizationOfBrutalForceEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_HAND_LIMIT;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2_OF_ANY_COLOR;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.AUTHORIZATION_OF_BRUTAL_FORCE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;

// The fuzzer's answers to decisions whose answers have a shape, found through the game's legal answers.
class CandidatesTest extends TestHarness {

    private final List<Finding> findings = new ArrayList<>();

    @Test
    void aDiscardDownToTheHandLimitTakesTheCardsOverIt() {
        player(1).character(LAURA_ROSLIN);
        for (int value = 1; value <= 12; value++) {
            player(1).skillCards().addOnTop(new SkillCard(value % 5, value % 2 == 0 ? EXECUTIVE_ORDER : CONSOLIDATE_POWER));
        }
        val decision = new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_DOWN_TO_HAND_LIMIT);

        for (int seed = 0; seed < 10; seed++) {
            val answer = (DiscardSkillCardsEvent) AnswerFinder.find(game, decision, new Random(seed), findings::add)
                    .orElseThrow();

            assertEquals(12 - player(1).handLimit(), answer.cards().size());
            assertEquals(DISCARD_DOWN_TO_HAND_LIMIT, answer.discardLimit());
            val hand = new ArrayList<>(player(1).skillCards().cards());
            assertTrue(answer.cards().stream().allMatch(hand::remove), "only cards of the hand");
        }
        assertEquals(List.of(), findings);
    }

    @Test
    void aDrawOfAnyColorLeavesOutAnEmptyDeck() {
        player(1).character(LAURA_ROSLIN);
        politicsDeck.clear();
        val decision = new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_2_OF_ANY_COLOR);

        for (int seed = 0; seed < 20; seed++) {
            val answer = (DrawSkillCardsEvent) AnswerFinder.find(game, decision, new Random(seed), findings::add)
                    .orElseThrow();

            assertEquals(2, answer.selection().values().stream().mapToInt(Integer::intValue).sum());
            assertFalse(answer.selection().containsKey(SkillCardColor.POLITICS));
        }
        assertEquals(List.of(), findings);
    }

    // "Authorization of Brutal Force" takes 3 raiders from any areas.
    @Test
    void aListOfShipsTakesThemFromAnyArea() {
        holdAuthorizationOfBrutalForce();
        val raiders = Set.of(raiderAt(GALACTICA_SPACE_2_OCLOCK).id(), raiderAt(GALACTICA_SPACE_8_OCLOCK).id(),
                raiderAt(GALACTICA_SPACE_12_OCLOCK).id());

        val answer = (AuthorizationOfBrutalForceEvent) AnswerFinder.find(game,
                new PlayerDecisionEvent<>(1, AuthorizationOfBrutalForceEvent.class), new Random(1), findings::add)
                .orElseThrow();

        assertEquals(raiders, Set.copyOf(answer.shipIds()));
        assertEquals(List.of(), findings);
    }

    // Or a centurion of the Boarding Party track.
    @Test
    void aListOfShipsTakesACenturion() {
        holdAuthorizationOfBrutalForce();
        val centurion = centurion();
        galacticaBoard.boardGalactica(centurion);

        val answer = (AuthorizationOfBrutalForceEvent) AnswerFinder.find(game,
                new PlayerDecisionEvent<>(1, AuthorizationOfBrutalForceEvent.class), new Random(1), findings::add)
                .orElseThrow();

        assertEquals(List.of(centurion.id()), answer.shipIds());
        assertEquals(List.of(), findings);
    }

    // "Impeachment" discards 5 Skill Cards (F-4dc6dd).
    @Test
    void aListOfSkillCardsTakesFive() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        player(1).mutinyCards().addOnTop(IMPEACHMENT);
        for (int value = 1; value <= 6; value++) {
            player(1).skillCards().addOnTop(new SkillCard(value % 3, value % 2 == 0 ? EXECUTIVE_ORDER : CONSOLIDATE_POWER));
        }

        val answer = (ImpeachmentEvent) AnswerFinder.find(game, new PlayerDecisionEvent<>(1, ImpeachmentEvent.class),
                new Random(1), findings::add).orElseThrow();

        assertEquals(5, answer.discards().size());
        assertEquals(List.of(), findings);
    }

    private void holdAuthorizationOfBrutalForce() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(AUTHORIZATION_OF_BRUTAL_FORCE);
    }

    // "Selfish" discards cards worth 20 strength, here the whole hand of 10 (F-316031).
    @Test
    void aListOfSkillCardsReachesAStrength() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(1).loyaltyCards().addOnTop(NOT_CYLON_SELFISH);
        for (int i = 0; i < 10; i++) {
            player(1).skillCards().addOnTop(new SkillCard(2, CONSOLIDATE_POWER));
        }

        val answer = (RevealPersonalGoalEvent) AnswerFinder.find(game,
                new PlayerDecisionEvent<>(1, RevealPersonalGoalEvent.class), new Random(1), findings::add).orElseThrow();

        assertEquals(10, answer.discards().size());
        assertEquals(List.of(), findings);
    }
}
