package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.FOOD_RATIONING;

class FoodRationingResultEventProcessorTest extends EventTestHarness<FoodRationingResultEvent> {

    @Test
    void onSixTheFleetGainsFoodAndTheCardLeavesTheGame() {
        rolled(6);

        execute(new FoodRationingResultEvent());

        assertFollowup(gain(FOOD, 1));
        assertEquals(List.of(FOOD_RATIONING), game.removedComponents().items());
        assertEquals(List.of(), quorumDeck.discardedCards());
    }

    @Test
    void lowerTheCardIsDiscarded() {
        rolled(5);

        execute(new FoodRationingResultEvent());

        assertEquals(List.of(FOOD_RATIONING), quorumDeck.discardedCards());
        assertEquals(List.of(), game.removedComponents().items());
    }
}
