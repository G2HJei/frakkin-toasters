package xyz.zlatanov.frakkintoasters.event.action.quorum;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsSeen;
import xyz.zlatanov.frakkintoasters.event.skillcheck.result.DiscardFromDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.CONSULT_THE_ORACLE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;

class ConsultTheOracleEventProcessorTest extends EventTestHarness<ConsultTheOracleEvent> {

    private static final SkillCard CONSOLIDATE_1 = new SkillCard(1, CONSOLIDATE_POWER);
    private static final SkillCard ORDER_2       = new SkillCard(2, EXECUTIVE_ORDER);

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(CONSULT_THE_ORACLE);
    }

    // Only the President learns the cards.
    @Test
    void thePresidentLooksThenDiscardsFromTheDestinyDeck() {
        destinyDeck.addOnTop(CONSOLIDATE_1, ORDER_2);
        val bottom = crisisDeck.cards().getLast();

        execute(new ConsultTheOracleEvent(1, CRISIS));

        assertEquals(List.of(new CardsSeen(1, CRISIS, List.of(bottom)),
                new CardsSeen(1, DESTINY, destinyDeck.cards())), seen());
        // With 2 cards left, both go: the only option.
        assertFollowup(all(new DiscardFromDestinyDeckEvent(1, destinyDeck.cards()),
                new DiscardPlayedQuorumCardEvent(CONSULT_THE_ORACLE)));
    }

    @Test
    void anEmptyDestinyDeckLeavesNothingToDiscard() {
        execute(new ConsultTheOracleEvent(1, CRISIS));

        assertEquals(List.of(CONSULT_THE_ORACLE), quorumDeck.discardedCards());
    }

    // The Loyalty and Motive decks can't be chosen (rule A2.3.13.1).
    @Test
    void notTheLoyaltyDeck() {
        loyaltyDeck.addOnTop(xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON);

        assertInvalid(new ConsultTheOracleEvent(1, LOYALTY));
    }

    @Test
    void notAnEmptyDeck() {
        assertInvalid(new ConsultTheOracleEvent(1, DESTINY));
    }

    private List<CardsSeen> seen() {
        return journal.facts().stream().filter(CardsSeen.class::isInstance).map(CardsSeen.class::cast).toList();
    }
}
