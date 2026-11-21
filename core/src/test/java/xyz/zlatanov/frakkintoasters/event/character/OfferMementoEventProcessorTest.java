package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.BRENDAN_HOTDOG_COSTANZA;

class OfferMementoEventProcessorTest extends EventTestHarness<OfferMementoEvent> {

    @Test
    void theOfferClearsTheTrigger() {
        player(1).character(BRENDAN_HOTDOG_COSTANZA);
        ops.galactica().decreaseResource(POPULATION, 1);

        execute(new OfferMementoEvent(1));

        assertFollowup(one(new MementoEvent(1), new NoOpEvent(1)));
        assertEquals(Map.of(), game.turn().triggeredAbilities());
        assertEquals(NONE, AbilityTriggers.check(game));
    }

    // Too few Piloting cards to draw: the trigger is cleared, nothing is offered.
    @Test
    void clearedEvenWhenHeCantUseIt() {
        player(1).character(BRENDAN_HOTDOG_COSTANZA);
        ops.galactica().decreaseResource(POPULATION, 1);
        pilotingDeck.clear();

        execute(new OfferMementoEvent(1));

        assertEquals(Map.of(), game.turn().triggeredAbilities());
    }

    @Test
    void onlyWhenTriggered() {
        player(1).character(BRENDAN_HOTDOG_COSTANZA);

        assertInvalid(new OfferMementoEvent(1));
    }
}
