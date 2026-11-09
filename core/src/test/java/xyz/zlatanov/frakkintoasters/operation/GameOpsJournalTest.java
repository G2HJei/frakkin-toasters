package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsDiscarded;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.CardsDrawn;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.OwnTurnStarted;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.DeckFact.DeckShuffled;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsGained;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.*;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.TitleFact.*;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.Player;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

class GameOpsJournalTest extends TestHarness {

    @Test
    void stateChangesAreRecordedAsFacts() {
        ops.changeStep(GameStep.RECEIVE_SKILLS);
        ops.changeCurrentPlayer(2);
        ops.startTurn();
        ops.actingPlayer(3);
        ops.resetActingPlayer();
        ops.titles().assignAdmiral(Character.values()[0]);
        ops.titles().assignCag(Character.values()[1]);
        ops.titles().assignPresident(Character.values()[2]);

        assertEquals(List.of(new StepChanged(GameStep.RECEIVE_SKILLS), new CurrentPlayerChanged(2), new TurnStarted(1),
                new OwnTurnStarted(2), new ActingPlayerChanged(3), new ActingPlayerChanged(null),
                new AdmiralAssigned(Character.values()[0]), new CagAssigned(Character.values()[1]),
                new PresidentAssigned(Character.values()[2])), journal.facts());
        assertEquals(GameStep.RECEIVE_SKILLS, game.turn().step());
        assertEquals(1, game.turn().number());
        assertEquals(1, game.player(2).turns());
        assertEquals(Character.values()[1], game.cag());
    }

    @Test
    void theNextTurnCountsForTheGameAndTheCurrentPlayer() {
        ops.startTurn();
        ops.changeCurrentPlayer(2);
        ops.startTurn();
        ops.changeCurrentPlayer(1);
        ops.startTurn();

        assertEquals(3, game.turn().number());
        assertEquals(List.of(2, 1, 0), game.players().stream().map(Player::turns).toList());
    }

    @Test
    void theFirstPlayerIsRandomAndRecorded() {
        val first = ops.chooseFirstPlayer();

        assertEquals(List.of(new CurrentPlayerChanged(first)), journal.facts());
        assertEquals(first, game.turn().currentPlayerNumber());
        assertTrue(first >= 1 && first <= 3);
    }

    @Test
    void aPlayedCrisisCardStaysWithTheTurnUntilItIsDiscarded() {
        val card = ops.player(1).drawCrisisCard();

        ops.playCrisisCard(card);

        assertEquals(new CrisisCardPlayed(card), journal.facts().getLast());
        assertEquals(List.of(card), game.turn().crisisCards());

        ops.discardCrisisCards();

        val facts = journal.facts();
        assertEquals(List.of(new CrisisCardsCleared(List.of(card)), new CardsDiscarded(DeckId.CRISIS, List.of(card))),
                facts.subList(facts.size() - 2, facts.size()));
        assertEquals(List.of(), game.turn().crisisCards());
        assertEquals(List.of(card), crisisDeck.discardedCards());
    }

    @Test
    void aTurnWithoutCrisisCardsDiscardsNothing() {
        ops.discardCrisisCards();

        assertEquals(List.of(), journal.facts());
    }

    @Test
    void discardedQuorumCardsLeaveThePresidentsHandForTheDiscardPile() {
        ops.titles().drawQuorumCard();
        ops.titles().drawQuorumCard();
        val discarded = presidentHand.cards().getFirst();
        val kept = presidentHand.cards().getLast();

        ops.titles().discardQuorumCards(List.of(discarded));

        val facts = journal.facts();
        assertEquals(List.of(new PresidentQuorumCardsLost(List.of(discarded)),
                new CardsDiscarded(DeckId.QUORUM, List.of(discarded))), facts.subList(facts.size() - 2, facts.size()));
        assertEquals(List.of(kept), presidentHand.cards());
        assertEquals(List.of(discarded), quorumDeck.discardedCards());
    }

    @Test
    void shuffleLogsTheNewOrderAndAppliesIt() {
        val before = new ArrayList<>(politicsDeck.cards());
        politicsDeck.discard(new SkillCard(1, CONSOLIDATE_POWER));

        ops.shuffleDeck(DeckId.POLITICS);

        val fact = (DeckShuffled) journal.facts().getFirst();
        assertEquals(DeckId.POLITICS, fact.deck());
        assertEquals(fact.drawOrder(), politicsDeck.cards());
        assertEquals(before.size() + 1, politicsDeck.size());
        assertEquals(0, politicsDeck.discardSize());
    }

    @Test
    void drawEmptyingAReshufflingDeckLogsTheReshuffle() {
        val top = politicsDeck.cards().getFirst();
        politicsDeck.clear();
        politicsDeck.addOnTop(top);
        val discarded = new SkillCard(2, CONSOLIDATE_POWER);
        politicsDeck.discard(discarded);

        ops.player(1).drawSkillCards(POLITICS);

        val facts = journal.facts();
        assertEquals(new CardsDrawn(DeckId.POLITICS, List.of(top)), facts.get(0));
        assertEquals(new DeckShuffled(DeckId.POLITICS, List.of(discarded)), facts.get(1));
        assertEquals(new CardsGained(1, PlayerPile.SKILL, List.of(top)), facts.get(2));
        assertEquals(3, facts.size());
        assertEquals(List.of(discarded), politicsDeck.cards());
        assertEquals(0, politicsDeck.discardSize());
    }

    @Test
    void startGameLogsGameStartedAndShufflesTheStartingDecks() {
        ops.startGame();

        val facts = journal.facts();
        assertEquals(new GameStarted(3, KOBOL), facts.getFirst());
        assertTrue(facts.stream().skip(1).allMatch(DeckShuffled.class::isInstance));
        assertTrue(facts.stream().skip(1).map(DeckShuffled.class::cast).anyMatch(f -> f.deck() == DeckId.MOTIVE));
    }

    @Test
    void motiveCardsComeFromTheGamesMotiveDeck() {
        val deck = game.decks().<MotiveCard>deck(DeckId.MOTIVE);
        val size = deck.size();
        ops.player(1).drawMotiveCards();
        assertEquals(size - 2, deck.size());
        assertEquals(2, game.player(1).motiveCards().size());
    }

    @Test
    void sameSeedGivesSameShuffle() {
        val a = Randomness.seeded(42).shuffled(List.of(1, 2, 3, 4, 5, 6, 7, 8));
        val b = Randomness.seeded(42).shuffled(List.of(1, 2, 3, 4, 5, 6, 7, 8));
        assertEquals(a, b);
    }
}
