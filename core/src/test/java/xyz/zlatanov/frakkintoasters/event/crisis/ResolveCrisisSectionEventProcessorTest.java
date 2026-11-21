package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.WATER_SABOTAGED;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.WIDESPREAD_STARVATION;

class ResolveCrisisSectionEventProcessorTest extends EventTestHarness<ResolveCrisisSectionEvent> {

    @Test
    void resolvesTheChosenSection() {
        execute(new ResolveCrisisSectionEvent(WIDESPREAD_STARVATION, TOP));

        assertFollowup(new LoseResourceEvent(FOOD, 2));
    }

    @Test
    void startsTheSkillCheckOnTheTopOfASkillCheckCrisis() {
        execute(new ResolveCrisisSectionEvent(WATER_SABOTAGED, TOP));

        assertSkillCheckTriggered(SkillCheckType.WATER_SABOTAGED);
    }

    @Test
    void notWithoutASection() {
        assertInvalid(new ResolveCrisisSectionEvent(WATER_SABOTAGED, null));
    }
}
