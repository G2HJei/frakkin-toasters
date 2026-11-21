package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.character.MoralCompassEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.WATER_SABOTAGED;
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

    // The top of a skill check crisis is the skill check.
    @Test
    void shouldStartTheSkillCheckOnTheTopOfASkillCheckCrisis() {
        execute(new ChooseCrisisSectionEvent(1, WATER_SABOTAGED, TOP));

        assertSkillCheckTriggered(SkillCheckType.WATER_SABOTAGED);
    }

    // The alternate instruction below it.
    @Test
    void shouldResolveTheAlternateInstructionOnTheBottomOfASkillCheckCrisis() {
        execute(new ChooseCrisisSectionEvent(1, WATER_SABOTAGED, BOTTOM));

        assertFollowup(new LoseResourceEvent(FOOD, 1));
    }

    // "Moral Compass": Karl "Helo" Agathon may change the choice to the other section, or let it resolve.
    @Test
    void heloMayChangeTheChoice() {
        player(1).character(SAUL_TIGH);
        player(2).character(KARL_HELO_AGATHON);

        execute(new ChooseCrisisSectionEvent(1, WIDESPREAD_STARVATION, TOP));

        assertFollowup(one(new MoralCompassEvent(2, WIDESPREAD_STARVATION, BOTTOM),
                new ResolveCrisisSectionEvent(WIDESPREAD_STARVATION, TOP)));
    }

    // He wouldn't change his own choice.
    @Test
    void notHisOwnChoice() {
        player(2).character(KARL_HELO_AGATHON);

        execute(new ChooseCrisisSectionEvent(2, WIDESPREAD_STARVATION, TOP));

        assertFollowup(new LoseResourceEvent(FOOD, 2));
    }
}
