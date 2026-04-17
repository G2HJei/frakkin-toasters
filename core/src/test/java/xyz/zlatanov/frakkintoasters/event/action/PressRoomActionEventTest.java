package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.Discard1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.DiscardDownTo1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

class PressRoomActionEventTest {

    @Test
    void shouldDraw1MutinyAndFollowup() {
        val game = Game.builder().build();
        val followup = new PressRoomActionEvent(1, 2).execute(game);
        assertEquals(1, game.player(2).mutinyCards().size());
        assertEquals(expectedFollowUp(), followup);
    }

    List<Followup> expectedFollowUp() {
        return followWith(
                all(
                        new PlayerDecisionEvent<>(2, DiscardDownTo1MutinyCardEvent.class)),
                one(
                        new PlayerDecisionEvent<>(1, Discard1MutinyCardEvent.class),
                        new NoOpEvent(1))
        );
    }

}