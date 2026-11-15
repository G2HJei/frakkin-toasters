package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;

import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.GALACTICA_FALLING_APART;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.STRATEGIC_PLANNING;

// "Roll a die. On a 6 or lower, -1 food."
class ResolveCrisisRollEventProcessorTest extends EventTestHarness<ResolveCrisisRollEvent> {

    @Test
    void shouldResolveTheEffectOnALowRoll() {
        rolled(6);

        execute(new ResolveCrisisRollEvent(GALACTICA_FALLING_APART, BOTTOM));

        assertFollowup(new LoseResourceEvent(FOOD, 1));
    }

    @Test
    void shouldDoNothingOnAHighRoll() {
        rolled(7);

        execute(new ResolveCrisisRollEvent(GALACTICA_FALLING_APART, BOTTOM));
    }

    // The cards played on the roll count (rule 13.1): -2 makes 7 a 5.
    @Test
    void shouldCountTheModifiersOfTheRoll() {
        rolled(7);
        ops.playOnDieRoll(STRATEGIC_PLANNING, -2);

        execute(new ResolveCrisisRollEvent(GALACTICA_FALLING_APART, BOTTOM));

        assertFollowup(new LoseResourceEvent(FOOD, 1));
    }
}
