package xyz.zlatanov.frakkintoasters.projection;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.engine.PendingInput;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.engine.answer.LegalAnswers;
import xyz.zlatanov.frakkintoasters.projection.GameProjection.*;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCardInPlay;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeDieRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_6_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

// One test per row of the visibility table: what the holder, another player and an observer see.
class GameProjectionTest extends TestHarness {

    private static final SkillCard CONSOLIDATE = new SkillCard(1, CONSOLIDATE_POWER);
    private static final SkillCard EXECUTIVE   = new SkillCard(2, EXECUTIVE_ORDER);
    private static final SkillCard PLANNING    = new SkillCard(3, STRATEGIC_PLANNING);

    private static final Viewer HOLDER   = Viewer.player(1);
    private static final Viewer OTHER    = Viewer.player(2);
    private static final Viewer OBSERVER = Viewer.observer();

    @BeforeEach
    void characters() {
        ops.player(1).selectCharacter(LAURA_ROSLIN);
        ops.player(2).selectCharacter(WILLIAM_ADAMA);
        ops.player(3).selectCharacter(KARA_STARBUCK_THRACE);
    }

    @Test
    void skillCardsShowToTheHolderAndTheirColorsToEveryone() {
        ops.player(1).receiveSkillCard(CONSOLIDATE);
        ops.player(1).receiveSkillCard(EXECUTIVE);
        ops.player(1).receiveSkillCard(new SkillCard(4, EXECUTIVE_ORDER));

        assertEquals(java.util.Set.of(CONSOLIDATE, EXECUTIVE, new SkillCard(4, EXECUTIVE_ORDER)),
                java.util.Set.copyOf(player(HOLDER, 1).skillCards().cards()));
        for (val viewer : List.of(OTHER, OBSERVER)) {
            val hand = player(viewer, 1).skillCards();
            assertNull(hand.cards());
            assertEquals(3, hand.count());
            assertEquals(Map.of(POLITICS, 1, LEADERSHIP, 2), hand.colors());
        }
    }

    @Test
    void loyaltyMutinySuperCrisisAndMotiveCardsShowToTheHolderAndTheRevealedOnesToEveryone() {
        loyaltyDeck.nextCard(NOT_CYLON, NOT_CYLON_SELFISH);
        ops.player(1).drawLoyaltyCards(2);
        ops.player(1).revealLoyaltyCard(NOT_CYLON_SELFISH);
        mutinyDeck.nextCard(MutinyCard.values()[0]);
        ops.player(1).drawMutinyCard();
        superCrisisDeck.nextCard(SuperCrisisCard.values()[0]);
        ops.player(1).drawSuperCrisisCard();

        val holder = player(HOLDER, 1);
        assertEquals(new HiddenCards<>(1, List.of(NOT_CYLON_SELFISH), List.of(NOT_CYLON)), holder.loyaltyCards());
        assertEquals(List.of(MutinyCard.values()[0]), holder.mutinyCards().cards());
        assertEquals(List.of(SuperCrisisCard.values()[0]), holder.superCrisisCards().cards());
        for (val viewer : List.of(OTHER, OBSERVER)) {
            val other = player(viewer, 1);
            assertEquals(new HiddenCards<>(1, List.of(NOT_CYLON_SELFISH), null), other.loyaltyCards());
            assertEquals(new HiddenCards<>(1, List.of(), null), other.mutinyCards());
            assertEquals(new HiddenCards<>(1, List.of(), null), other.superCrisisCards());
            assertEquals(new HiddenCards<MotiveCard>(0, List.of(), null), other.motiveCards());
        }
    }

    @Test
    void theMutineerCardIsFaceupOnceSomeoneHoldsIt() {
        loyaltyDeck.nextCard(NOT_CYLON, MUTINEER);
        ops.player(2).drawLoyaltyCards(2);

        for (val viewer : List.of(HOLDER, OBSERVER)) {
            val mutineer = player(viewer, 2);
            assertTrue(mutineer.mutineer());
            assertEquals(new HiddenCards<>(1, List.of(MUTINEER), null), mutineer.loyaltyCards());
        }
    }

    @Test
    void thePresidentsQuorumCardsShowToThePresident() {
        ops.titles().assignPresident(LAURA_ROSLIN);
        quorumDeck.nextCard(QuorumCard.values()[0]);
        ops.titles().drawQuorumCard();

        assertEquals(new HiddenCards<>(1, List.of(), List.of(QuorumCard.values()[0])), projection(HOLDER).presidentHand());
        assertEquals(new HiddenCards<QuorumCard>(1, List.of(), null), projection(OTHER).presidentHand());
        assertEquals(new HiddenCards<QuorumCard>(1, List.of(), null), projection(OBSERVER).presidentHand());
    }

    @Test
    void theQuorumCardsInPlayShowToEveryone() {
        ops.titles().putInPlay(QuorumCard.PROBATION, 2);

        assertEquals(List.of(new QuorumCardInPlay(QuorumCard.PROBATION, 2)), projection(OBSERVER).quorumCardsInPlay());
    }

    @Test
    void theLoyaltyCardsAPlayerLookedAtShowOnlyToHim() {
        loyaltyDeck.nextCard(NOT_CYLON);
        ops.player(2).drawLoyaltyCards(1);
        ops.player(1).lookAtLoyaltyCard(2);

        assertEquals(1, player(HOLDER, 1).seenLoyaltyCards().size());
        assertNull(player(OTHER, 1).seenLoyaltyCards());
        assertNull(player(OBSERVER, 1).seenLoyaltyCards());
    }

    @Test
    void decksShowTheirCountsAndTheCardsOfAFaceupDiscardPile() {
        ops.player(1).receiveSkillCard(CONSOLIDATE);
        ops.player(1).discard(CONSOLIDATE);

        for (val viewer : List.of(HOLDER, OTHER, OBSERVER)) {
            val politics = deck(viewer, DeckId.POLITICS);
            assertEquals(List.of(CONSOLIDATE), politics.discarded());
            assertEquals(1, politics.discardSize());
            assertNull(politics.drawPile());
            assertNull(deck(viewer, DeckId.LOYALTY).discarded());
            assertEquals(game.decks().deck(DeckId.CRISIS).size(), deck(viewer, DeckId.CRISIS).size());
        }
    }

    @Test
    void cardsPlayedBeforeASkillCheckShowAsACountPerPlayerUntilTheyAreRevealed() {
        ops.skillCheck().start(SkillCheckType.values()[0]);
        ops.player(1).receiveSkillCard(CONSOLIDATE);
        ops.skillCheck().playBeforeSkillCheck(1, List.of(CONSOLIDATE));

        assertEquals(List.of(new PlayedBefore(1, new SkillHand(1, Map.of(POLITICS, 1), List.of(CONSOLIDATE)))),
                projection(HOLDER).skillCheck().playedBefore());
        for (val viewer : List.of(OTHER, OBSERVER)) {
            assertEquals(List.of(new PlayedBefore(1, new SkillHand(1, Map.of(POLITICS, 1), null))),
                    projection(viewer).skillCheck().playedBefore());
        }

        ops.skillCheck().revealPlayedBefore();

        assertEquals(List.of(CONSOLIDATE), projection(OBSERVER).skillCheck().playedBefore().getFirst().cards().cards());
    }

    @Test
    void cardsInAnOpenInterruptWindowShowToThePlayerWhoPlayedThemAndStayInHisHandForTheOthers() {
        ops.player(1).receiveSkillCard(PLANNING);
        ops.player(1).receiveSkillCard(CONSOLIDATE);
        ops.interrupts().openWindow(new BeforeDieRoll());
        val played = new PlayedInterruptCard(1, PLANNING, null);
        ops.interrupts().play(played);

        assertEquals(List.of(played), projection(HOLDER).turn().windowCards());
        assertEquals(List.of(CONSOLIDATE), player(HOLDER, 1).skillCards().cards());
        for (val viewer : List.of(OTHER, OBSERVER)) {
            assertEquals(List.of(), projection(viewer).turn().windowCards());
            assertEquals(new SkillHand(2, Map.of(TACTICS, 1, POLITICS, 1), null), player(viewer, 1).skillCards());
        }
    }

    @Test
    void theCargoOfACivilianShipShowsToThePlayersWhoLookedAtIt() {
        civilianShips.nextCard(new CivilianShip(2001, 1, 0, 2));
        ops.galactica().placeCivilianShip(GALACTICA_SPACE_6_OCLOCK);
        ops.player(1).lookAtShip(2001);

        assertEquals(new Cargo(1, 0, 2), civilianShip(HOLDER).cargo());
        assertEquals(List.of(2001), player(HOLDER, 1).knownCivilianShips());
        for (val viewer : List.of(OTHER, OBSERVER)) {
            assertNull(civilianShip(viewer).cargo());
            assertEquals(2001, civilianShip(viewer).id());
        }
    }

    @Test
    void thePendingInputGoesToThePlayerWhoAnswersIt() {
        val decision = new AwaitingDecision(new PlayerDecisionEvent<>(1, MoveEvent.class));

        val moves = LegalAnswers.of(game, decision.decision());
        assertEquals(new Decide(1, MoveEvent.class, List.of(), moves), projection(HOLDER, decision).input());
        assertEquals(new WaitingFor(1), projection(OTHER, decision).input());
        assertEquals(new WaitingFor(1), projection(OBSERVER, decision).input());

        // the second option asks for the move: its answers come with it
        val options = new LinkedHashSet<Followup>(List.of(single(new NoOpEvent(1)), single(new PlayerDecisionEvent<>(1, MoveEvent.class))));
        assertEquals(new Choose(1, List.copyOf(options), List.of(new OptionAnswers(1, moves))),
                projection(HOLDER, new AwaitingChoice(1, options)).input());
    }

    @Test
    void theAnswersOfADecisionAreTheGamesOwn() {
        moveTo(ADMIRALS_QUARTERS, LAURA_ROSLIN);
        val decision = new AwaitingDecision(new PlayerDecisionEvent<>(1, MoveEvent.class));

        val answers = ((Decide) projection(HOLDER, decision).input()).answers();

        assertTrue(answers.events().contains(new MoveEvent(1, RESEARCH_LAB, null)));
        assertFalse(answers.isEmpty());
    }

    @Test
    void nobodyElseLearnsWhoAnswersASecretInput() {
        val secret = new AwaitingDecision(new PlayerDecisionEvent<>(1, PlayInterruptCardEvent.class));

        assertEquals(new Decide(1, PlayInterruptCardEvent.class, List.of(), LegalAnswers.of(game, secret.decision())),
                projection(HOLDER, secret).input());
        assertEquals(new Waiting(), projection(OTHER, secret).input());
        assertEquals(new Waiting(), projection(OBSERVER, secret).input());
    }

    @Test
    void everythingShowsToEveryoneOnceTheGameHasEnded() {
        ops.player(1).receiveSkillCard(CONSOLIDATE);
        loyaltyDeck.nextCard(NOT_CYLON);
        ops.player(1).drawLoyaltyCards(1);
        ops.endGame(Team.HUMANS, List.of(1, 2, 3));

        val observed = projection(OBSERVER);
        assertEquals(new Outcome(Team.HUMANS, List.of(1, 2, 3)), observed.outcome());
        assertNull(observed.input());
        assertEquals(List.of(CONSOLIDATE), observed.players().getFirst().skillCards().cards());
        assertEquals(List.of(NOT_CYLON), observed.players().getFirst().loyaltyCards().cards());
    }

    // The sandbox's one user plays every seat: every hidden item of the table shows, the draw piles too.
    @Test
    void theSandboxSeesEverythingHidden() {
        ops.player(1).receiveSkillCard(CONSOLIDATE);
        loyaltyDeck.nextCard(NOT_CYLON, NOT_CYLON_SELFISH);
        ops.player(2).drawLoyaltyCards(1);
        ops.player(3).lookAtLoyaltyCard(2);
        ops.titles().assignPresident(LAURA_ROSLIN);
        quorumDeck.nextCard(QuorumCard.values()[0]);
        ops.titles().drawQuorumCard();
        civilianShips.nextCard(new CivilianShip(2001, 1, 0, 2));
        ops.galactica().placeCivilianShip(GALACTICA_SPACE_6_OCLOCK);
        ops.player(2).receiveSkillCard(PLANNING);
        ops.interrupts().openWindow(new BeforeDieRoll());
        val played = new PlayedInterruptCard(2, PLANNING, null);
        ops.interrupts().play(played);
        val secret = new AwaitingDecision(new PlayerDecisionEvent<>(3, PlayInterruptCardEvent.class));

        val sandbox = projection(Viewer.sandbox(), secret);

        assertEquals(List.of(CONSOLIDATE), sandbox.players().get(0).skillCards().cards());
        assertEquals(List.of(NOT_CYLON), sandbox.players().get(1).loyaltyCards().cards());
        assertEquals(1, sandbox.players().get(2).seenLoyaltyCards().size());
        assertEquals(List.of(QuorumCard.values()[0]), sandbox.presidentHand().cards());
        assertEquals(new Cargo(1, 0, 2), civilianShip(Viewer.sandbox()).cargo());
        assertEquals(List.of(played), sandbox.turn().windowCards());
        // a secret input in full
        assertEquals(new Decide(3, PlayInterruptCardEvent.class, List.of(), LegalAnswers.of(game, secret.decision())),
                sandbox.input());
        assertEquals(game.decks().deck(DeckId.CRISIS).cards(), sandbox.decks().stream()
                .filter(deck -> deck.deck() == DeckId.CRISIS).findFirst().orElseThrow().drawPile());
    }

    private GameProjection projection(Viewer viewer) {
        return GameProjection.of(game, Optional.empty(), viewer);
    }

    private GameProjection projection(Viewer viewer, PendingInput input) {
        return GameProjection.of(game, Optional.of(input), viewer);
    }

    private PlayerProjection player(Viewer viewer, int number) {
        return projection(viewer).players().get(number - 1);
    }

    private DeckProjection deck(Viewer viewer, DeckId id) {
        return projection(viewer).decks().stream().filter(deck -> deck.deck() == id).findFirst().orElseThrow();
    }

    private ShipProjection civilianShip(Viewer viewer) {
        return projection(viewer).galactica().ships().stream()
                .filter(ship -> ship.type() == xyz.zlatanov.frakkintoasters.state.ship.ShipType.CIVILIAN)
                .findFirst()
                .orElseThrow();
    }
}
