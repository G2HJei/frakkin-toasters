package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.INSPIRATIONAL_SPEECH;

class InspirationalSpeechResultEventProcessorTest extends EventTestHarness<InspirationalSpeechResultEvent> {

    @Test
    void onEightTheFleetGainsMoraleAndTheCardLeavesTheGame() {
        rolled(8);

        execute(new InspirationalSpeechResultEvent());

        assertFollowup(gain(MORALE, 1));
        assertEquals(List.of(INSPIRATIONAL_SPEECH), game.removedComponents().items());
    }

    @Test
    void lowerTheCardIsDiscarded() {
        rolled(1);

        execute(new InspirationalSpeechResultEvent());

        assertEquals(List.of(INSPIRATIONAL_SPEECH), quorumDeck.discardedCards());
    }
}
