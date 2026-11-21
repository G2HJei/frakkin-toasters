package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.quorum.EstablishDogsvilleEvent;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.DiscardHeldQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESIDENTS_OFFICE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class SkilledPoliticianChoiceEventProcessorTest extends EventTestHarness<SkilledPoliticianChoiceEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    @Test
    void holdsTheChosenCardToPlayIt() {
        execute(new SkilledPoliticianChoiceEvent(1, ESTABLISH_DOGSVILLE, List.of(EULOGY, ARREST_ORDER)));

        assertEquals(List.of(ESTABLISH_DOGSVILLE), player(1).quorumCards().cards());
        assertEquals(ARREST_ORDER, game.decks().deck(xyz.zlatanov.frakkintoasters.state.deck.DeckId.QUORUM).bottom());
        assertFollowup(new PlayerDecisionEvent<>(1, EstablishDogsvilleEvent.class));
    }

    // "Eulogy" needs morale lost to an execution earlier in the game.
    @Test
    void aCardThatCantBePlayedIsDiscarded() {
        execute(new SkilledPoliticianChoiceEvent(1, EULOGY, List.of()));

        assertFollowup(new DiscardHeldQuorumCardEvent(1, EULOGY));
    }
}
