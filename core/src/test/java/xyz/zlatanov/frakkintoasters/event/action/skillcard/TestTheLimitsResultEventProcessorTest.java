package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.damageGalactica;

class TestTheLimitsResultEventProcessorTest extends EventTestHarness<TestTheLimitsResultEvent> {

    // The current player may draw a Pegasus damage token instead (rule 11.7.2).
    @Test
    void onFiveGalacticaIsDamaged() {
        rolled(5);

        execute(new TestTheLimitsResultEvent());

        assertFollowup(damageGalactica());
    }

    @Test
    void higherNothingHappens() {
        rolled(6);

        execute(new TestTheLimitsResultEvent());
    }
}
