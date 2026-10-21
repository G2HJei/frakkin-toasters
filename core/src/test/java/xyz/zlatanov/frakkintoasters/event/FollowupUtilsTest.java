package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.RevealCardsBeforeSkillCheckEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;

class FollowupUtilsTest extends TestHarness {

    @Test
    void shouldBuildAllFollowupEvents() {
        assertEquals(
                all(
                        new PlayerDecisionEvent<>(2, PlayCardsBeforeSkillCheckEvent.class),
                        new PlayerDecisionEvent<>(3, PlayCardsBeforeSkillCheckEvent.class),
                        new PlayerDecisionEvent<>(1, PlayCardsBeforeSkillCheckEvent.class),
                        new RevealCardsBeforeSkillCheckEvent()
                ),
                FollowupUtils.beforeSkillCheckFollowups(game));
    }
}