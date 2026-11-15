package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

// "The current player may move 1 character from the "Brig" to any other location." (on Galactica, appendix A2.3.9.3)
class StandoffWithPegasusPassEventProcessorTest extends EventTestHarness<StandoffWithPegasusPassEvent> {

    @Test
    void shouldLetTheCurrentPlayerMoveSomeoneFromTheBrig() {
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(BRIG, KARA_STARBUCK_THRACE);

        execute(new StandoffWithPegasusPassEvent());

        assertFollowup(one(new PlayerDecisionEvent<>(1, MoveFromBrigEvent.class), new NoOpEvent(1)));
    }

    // The decision would have no answer.
    @Test
    void shouldAskNobodyWhenTheBrigIsEmpty() {
        execute(new StandoffWithPegasusPassEvent());
    }
}
