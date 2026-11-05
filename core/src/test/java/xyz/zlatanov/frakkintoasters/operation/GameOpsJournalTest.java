package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.journal.fact.DeckFact.DeckShuffled;
import xyz.zlatanov.frakkintoasters.journal.fact.GameFact.*;
import xyz.zlatanov.frakkintoasters.journal.fact.TitleFact.*;
import xyz.zlatanov.frakkintoasters.state.GameStep;
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
        ops.startTurn(3);
        ops.changeCurrentPlayer(2);
        ops.actingPlayer(3);
        ops.resetActingPlayer();
        ops.titles().assignAdmiral(Character.values()[0]);
        ops.titles().assignCag(Character.values()[1]);
        ops.titles().assignPresident(Character.values()[2]);

        assertEquals(List.of(new StepChanged(GameStep.RECEIVE_SKILLS), new TurnStarted(3), new CurrentPlayerChanged(2),
                new ActingPlayerChanged(3), new ActingPlayerChanged(null), new AdmiralAssigned(Character.values()[0]),
                new CagAssigned(Character.values()[1]), new PresidentAssigned(Character.values()[2])), game.journal().facts());
        assertEquals(GameStep.RECEIVE_SKILLS, game.step());
        assertEquals(3, game.turn());
        assertEquals(Character.values()[1], game.cag());
    }

    @Test
    void shuffleLogsTheNewOrderAndAppliesIt() {
        val before = new ArrayList<>(politicsDeck.cards());
        politicsDeck.discard(new SkillCard(1, CONSOLIDATE_POWER));

        ops.shuffleDeck(DeckId.POLITICS);

        val fact = (DeckShuffled) game.journal().facts().getFirst();
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

        assertEquals(List.of(new DeckShuffled(DeckId.POLITICS, List.of(discarded))), game.journal().facts());
        assertEquals(List.of(discarded), politicsDeck.cards());
        assertEquals(0, politicsDeck.discardSize());
    }

    @Test
    void startGameLogsGameStartedAndShufflesTheStartingDecks() {
        ops.startGame();

        val facts = game.journal().facts();
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
