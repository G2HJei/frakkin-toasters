package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.FOOD_RATIONING;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class FoodRationingEventProcessorTest extends EventTestHarness<FoodRationingEvent> {

    // The card is in no pile until the roll decides where it goes.
    @Test
    void thePresidentRolls() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(FOOD_RATIONING);

        execute(new FoodRationingEvent(1));

        assertEquals(List.of(), presidentHand.cards());
        assertEquals(List.of(), quorumDeck.discardedCards());
        assertFollowup(rollDie(new FoodRationingResultEvent()));
    }
}
