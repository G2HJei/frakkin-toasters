package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_HAND_LIMIT;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ARREST_ORDER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class DiscardQuorumCardsEventProcessorTest extends EventTestHarness<DiscardQuorumCardsEvent> {

    private final List<QuorumCard> hand = Arrays.asList(QuorumCard.values()).subList(0, 11);

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(hand);
    }

    @Test
    void thePresidentDiscardsChosenCards() {
        execute(new DiscardQuorumCardsEvent(1, List.of(hand.get(0), hand.get(1))));

        assertEquals(hand.subList(2, 11), presidentHand.cards());
        assertEquals(List.of(hand.get(0), hand.get(1)), quorumDeck.discardedCards());
    }

    @Test
    void thePresidentDiscardsDownToTheHandLimit() {
        execute(new DiscardQuorumCardsEvent(1, List.of(hand.get(3)), DISCARD_DOWN_TO_HAND_LIMIT));

        assertEquals(10, presidentHand.size());
    }

    @Test
    void shouldRejectStayingOverTheHandLimit() {
        assertInvalid(new DiscardQuorumCardsEvent(1, List.of(), DISCARD_DOWN_TO_HAND_LIMIT));
    }

    @Test
    void onlyThePresidentHoldsQuorumCards() {
        assertInvalid(new DiscardQuorumCardsEvent(2, List.of(hand.get(0))));
    }

    @Test
    void shouldRejectACardNotInHand() {
        presidentHand.remove(ARREST_ORDER);

        assertInvalid(new DiscardQuorumCardsEvent(1, List.of(ARREST_ORDER)));
    }

    @Test
    void shouldRejectACardOnceMoreThanInHand() {
        assertInvalid(new DiscardQuorumCardsEvent(1, List.of(hand.get(0), hand.get(0))));
    }

    @Test
    void discardingNothingChangesNothing() {
        execute(new DiscardQuorumCardsEvent(1, List.of()));

        assertEquals(hand, presidentHand.cards());
        assertEquals(List.of(), journal.facts());
    }
}
