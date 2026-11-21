package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;

class CylonPowerDecreaseJumpTrackEventProcessorTest extends EventTestHarness<CylonPowerDecreaseJumpTrackEvent> {

    @Test
    void shouldMoveTheJumpTrackBackTwice() {
        execute(new CylonPowerDecreaseJumpTrackEvent(1));

        assertFollowup(all(new DecreaseJumpPreparationEvent(), new DecreaseJumpPreparationEvent()));
    }
}
