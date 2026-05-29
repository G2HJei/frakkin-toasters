package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.*;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

class PressRoomActionEventTest extends EventTest {

    @Test
    void shouldDraw1MutinyAndFollowup() {
        val followup = new PressRoomActionEvent(1, 2).execute(game);
        assertEquals(1, player(2).mutinyCards().size());
        assertEquals(expectedFollowUp(), followup);
    }

    Followup expectedFollowUp() {
        return all(
                single(new PlayerDecisionEvent<>(2, DiscardDownTo1MutinyCardEvent.class)),
                one(
                        new PlayerDecisionEvent<>(1, Discard1MutinyCardEvent.class),
                        new NoOpEvent(1))
        );
    }

}
