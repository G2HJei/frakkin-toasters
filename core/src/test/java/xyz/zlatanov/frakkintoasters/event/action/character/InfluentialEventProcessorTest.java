package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESS_ROOM;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TORY_FOSTER;

class InfluentialEventProcessorTest extends EventTestHarness<InfluentialEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(TORY_FOSTER);
        moveTo(PRESS_ROOM, TORY_FOSTER);
    }

    @Test
    void looksAtTheTopQuorumCardsToReorderThem() {
        execute(new InfluentialEvent(1));

        assertFollowup(new PlayerDecisionEvent<>(1, ReorderTopQuorumCardsEvent.class));
    }

    @Test
    void notWithAnEmptyQuorumDeck() {
        quorumDeck.clear();

        assertInvalid(new InfluentialEvent(1));
    }
}
