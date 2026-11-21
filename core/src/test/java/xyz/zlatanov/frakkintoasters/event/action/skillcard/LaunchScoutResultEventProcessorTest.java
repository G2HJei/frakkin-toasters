package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINATION;

class LaunchScoutResultEventProcessorTest extends EventTestHarness<LaunchScoutResultEvent> {

    @Test
    void onThreeThePlayerLooksAtACard() {
        rolled(3);

        execute(new LaunchScoutResultEvent(1));

        assertFollowup(one(new LookAtTopCardEvent(1, CRISIS), new LookAtTopCardEvent(1, DESTINATION)));
    }

    @Test
    void lowerTheRaptorIsDestroyed() {
        rolled(2);

        execute(new LaunchScoutResultEvent(1));

        assertFollowup(new DestroyRaptorEvent());
    }
}
