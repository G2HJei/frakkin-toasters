package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.journal.fact.DeckFact.CardsDiscarded;
import xyz.zlatanov.frakkintoasters.journal.fact.SkillCheckFact.*;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckPile;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

class SkillCheckOpsJournalTest extends TestHarness {

    SkillCheckType type  = SkillCheckType.values()[0];
    SkillCard      card  = new SkillCard(3, CONSOLIDATE_POWER);
    SkillCheckPile pile = type.skillCheckColors().contains(POLITICS) ? SkillCheckPile.MATCHING : SkillCheckPile.NON_MATCHING;

    @Test
    void playedCardsLeaveTheHandAndLandInAPile() {
        game.player(1).gainSkillCards(card);

        ops.skillCheck().start(type, 2);
        ops.skillCheck().playCards(1, card);

        assertEquals(List.of(
                new SkillCheckStarted(type),
                new TargetChosen(2),
                new xyz.zlatanov.frakkintoasters.journal.fact.PlayerFact.CardsLost(1, PlayerPile.SKILL, List.of(card)),
                new CardsPlaced(pile, List.of(card), true)), game.journal().facts());
        assertNoSkillCards(1);
        assertEquals(List.of(card), game.activeSkillCheck().pile(pile).cards());
    }

    @Test
    void discardingFromTheCheckLogsTheRemovalAndTheDiscard() {
        game.player(1).gainSkillCards(card);
        ops.skillCheck().start(type);
        ops.skillCheck().playCards(1, card);
        val before = game.journal().facts().size();

        ops.skillCheck().discard(card);

        assertEquals(List.of(new CardRemoved(card, null), new CardsDiscarded(DeckId.POLITICS, List.of(card))),
                game.journal().facts().subList(before, game.journal().facts().size()));
        assertEquals(List.of(card), politicsDeck.discardedCards());
    }

    @Test
    void dividingTheCardsLogsWhatWasDrawnAndWhereItWent() {
        val second = new SkillCard(1, CONSOLIDATE_POWER);
        destinyDeck.addOnTop(card, second);
        ops.skillCheck().start(type);
        ops.skillCheck().playFromDestinyDeck(2);
        val before = game.journal().facts().size();

        ops.skillCheck().shuffleAndDivideCards();

        assertEquals(List.of(
                new CardsDrawn(SkillCheckPile.CARDS, List.of(card, second)),
                new CardsPlaced(pile, List.of(card), false),
                new CardsPlaced(pile, List.of(second), false)),
                game.journal().facts().subList(before, game.journal().facts().size()));
        assertEquals(List.of(card, second), game.activeSkillCheck().pile(pile).cards());
    }
}
