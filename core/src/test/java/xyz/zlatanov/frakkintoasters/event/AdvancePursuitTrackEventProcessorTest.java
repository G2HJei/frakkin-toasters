package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.track.Pursuit.POSITION_2;

class AdvancePursuitTrackEventProcessorTest extends EventTestHarness<AdvancePursuitTrackEvent> {

    @Test
    void shouldAdvancePursuitTrack() {
        cylonFleetBoard.advancePursuit();
        executeAndAssertNoFollowup(new AdvancePursuitTrackEvent());
        assertEquals(POSITION_2, cylonFleetBoard.pursuitTrack());
    }
}