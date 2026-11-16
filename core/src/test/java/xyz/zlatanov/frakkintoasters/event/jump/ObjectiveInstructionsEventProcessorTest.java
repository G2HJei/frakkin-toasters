package xyz.zlatanov.frakkintoasters.event.jump;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.loyalty.SleeperAgentPhaseEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.DESOLATE_MOON;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.ICY_MOON;

// "If the previous step causes the total distance to equal or exceed the given distances for the first time, then follow
// the instructions on the Objective Card." With the Kobol Objective that is the Sleeper Agent phase at 4 distance.
class ObjectiveInstructionsEventProcessorTest extends EventTestHarness<ObjectiveInstructionsEvent> {

    @Test
    void theSleeperAgentPhaseStartsWhenTheDistanceReachesFour() {
        ops.galactica().reachDestination(DESOLATE_MOON);
        ops.galactica().reachDestination(ICY_MOON);

        execute(new ObjectiveInstructionsEvent(3));

        assertFollowup(new SleeperAgentPhaseEvent());
    }

    // "Legendary Discovery": "Place this card next to the Kobol Objective card. It counts as 1 distance."
    @Test
    void aLegendaryDiscoveryCountsAsOneDistance() {
        ops.galactica().reachDestination(DESOLATE_MOON);
        ops.keepCrisisCardInPlay(SkillCheckType.LEGENDARY_DISCOVERY);

        assertEquals(4, game.distanceTraveled());
        execute(new ObjectiveInstructionsEvent(3));

        assertFollowup(new SleeperAgentPhaseEvent());
    }

    @Test
    void notAgainOnceItWasReached() {
        ops.galactica().reachDestination(DESOLATE_MOON);
        ops.galactica().reachDestination(DESOLATE_MOON);

        execute(new ObjectiveInstructionsEvent(4));
    }

    @Test
    void notBeforeFour() {
        ops.galactica().reachDestination(DESOLATE_MOON);

        execute(new ObjectiveInstructionsEvent(0));
    }
}
