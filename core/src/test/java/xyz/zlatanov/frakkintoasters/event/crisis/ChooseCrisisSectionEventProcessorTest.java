package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.WIDESPREAD_STARVATION;

class ChooseCrisisSectionEventProcessorTest extends EventTestHarness<ChooseCrisisSectionEvent> {

    @Test
    void shouldResolveTheTopSection() {
        execute(new ChooseCrisisSectionEvent(2, WIDESPREAD_STARVATION, TOP));

        assertFollowup(new LoseResourceEvent(FOOD, 2));
    }

    @Test
    void shouldResolveTheBottomSection() {
        execute(new ChooseCrisisSectionEvent(2, WIDESPREAD_STARVATION, BOTTOM));

        assertFollowup(all(new LoseResourceEvent(FOOD, 1), new LoseResourceEvent(POPULATION, 1)));
    }
}
