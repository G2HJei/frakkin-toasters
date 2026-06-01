package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.Discard1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.DiscardDownTo1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

class PressRoomEventProcessorTest extends EventTestHarness<PressRoomEvent> {

    @Test
    void shouldDraw1MutinyAndFollowup() {
        executeAndAssertFollowup(new PressRoomEvent(1, 2),
                all(
                        single(
                                new PlayerDecisionEvent<>(2, DiscardDownTo1MutinyCardEvent.class)),
                        one(
                                new PlayerDecisionEvent<>(1, Discard1MutinyCardEvent.class),
                                new NoOpEvent(1))
                ));
        assertEquals(1, player(2).mutinyCards().size());
    }

}
