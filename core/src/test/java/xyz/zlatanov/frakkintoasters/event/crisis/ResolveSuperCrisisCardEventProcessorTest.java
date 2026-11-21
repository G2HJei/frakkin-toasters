package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.FIGHTING_BLIND;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.HUMAN_PRISONER;

class ResolveSuperCrisisCardEventProcessorTest extends EventTestHarness<ResolveSuperCrisisCardEvent> {

    @BeforeEach
    void setUpTitles() {
        player(1).character(FELIX_GAETA);
        player(2).character(LEE_APOLLO_ADAMA);
        player(3).character(LAURA_ROSLIN);
        cag(LEE_APOLLO_ADAMA);
        president(LAURA_ROSLIN);
        admiral(LAURA_ROSLIN);
    }

    @Test
    void shouldStartTheSkillCheckAndRememberWhoPlayedTheCard() {
        execute(new ResolveSuperCrisisCardEvent(1, HUMAN_PRISONER));

        assertEquals(HUMAN_PRISONER.skillCheck(), game.activeSkillCheck().type());
        assertEquals(1, game.activeSkillCheck().playedBy());
        assertTrue(game.activeSkillCheck().type().isSuperCrisis());
        assertFollowup(xyz.zlatanov.frakkintoasters.event.FollowupUtils.beforeSkillCheckFollowups(game));
    }

    @Test
    void shouldLetTheTitleHolderChooseASection() {
        execute(new ResolveSuperCrisisCardEvent(1, FIGHTING_BLIND));

        assertNull(game.activeSkillCheck());
        assertFollowup(one(new ChooseSuperCrisisSectionEvent(2, FIGHTING_BLIND, TOP),
                new ChooseSuperCrisisSectionEvent(2, FIGHTING_BLIND, BOTTOM)));
    }

    @Test
    void shouldRejectNoCard() {
        assertInvalid(new ResolveSuperCrisisCardEvent(1, null));
    }
}
