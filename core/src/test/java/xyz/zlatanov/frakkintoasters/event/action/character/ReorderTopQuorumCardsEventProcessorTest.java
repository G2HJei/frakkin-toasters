package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TORY_FOSTER;

class ReorderTopQuorumCardsEventProcessorTest extends EventTestHarness<ReorderTopQuorumCardsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(TORY_FOSTER);
        quorumDeck.clear();
        quorumDeck.nextCard(EULOGY, ARREST_ORDER, PROBATION).top(3);
    }

    @Test
    void putsTheTopCardsBackInHerOrder() {
        execute(new ReorderTopQuorumCardsEvent(1, List.of(PROBATION, EULOGY, ARREST_ORDER)));

        assertEquals(List.of(PROBATION, EULOGY, ARREST_ORDER), game.decks().deck(DeckId.QUORUM).cards());
    }

    @Test
    void notOtherCards() {
        assertInvalid(new ReorderTopQuorumCardsEvent(1, List.of(PROBATION, EULOGY, EULOGY)));
    }
}
