package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class HumanPrisonerFailEventProcessorTest extends EventTestHarness<HumanPrisonerFailEvent> {

    @BeforeEach
    void setUpCylon() {
        player(1).character(FELIX_GAETA);
        player(2).character(LAURA_ROSLIN);
        player(3).character(WILLIAM_ADAMA);
        revealCylon();
    }

    // The Cylon who played the card picks one of the humans, clockwise from himself.
    @Test
    void shouldLetTheCylonWhoPlayedTheCardPickAHuman() {
        ops.skillCheck().startSuperCrisis(SkillCheckType.HUMAN_PRISONER, 1);

        execute(new HumanPrisonerFailEvent());

        assertFollowup(one(new HumanPrisonerTakeHandEvent(1, 2), new HumanPrisonerTakeHandEvent(1, 3)));
    }

    @Test
    void shouldDoNothingWhenNobodyPlayedTheCard() {
        ops.skillCheck().start(SkillCheckType.HUMAN_PRISONER);
        execute(new HumanPrisonerFailEvent());
    }
}
