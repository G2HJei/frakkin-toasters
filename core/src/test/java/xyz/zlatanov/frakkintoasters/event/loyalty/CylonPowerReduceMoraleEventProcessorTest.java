package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;

class CylonPowerReduceMoraleEventProcessorTest extends EventTestHarness<CylonPowerReduceMoraleEvent> {

    @Test
    void shouldLoseOneMorale() {
        execute(new CylonPowerReduceMoraleEvent(1));

        assertFollowup(lose(MORALE, 1));
    }
}
