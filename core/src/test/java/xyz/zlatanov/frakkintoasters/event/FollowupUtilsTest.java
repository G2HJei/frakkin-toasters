package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;

class FollowupUtilsTest extends TestHarness {

    @Test
    void shouldBuildAllFollowupEvents() {
        assertEquals(
                all(
                        new PlayFromDestinyDeckEvent(),
                        new PlayerDecisionEvent<>(2, PlayCardsBeforeSkillCheckEvent.class),
                        new PlayerDecisionEvent<>(3, PlayCardsBeforeSkillCheckEvent.class),
                        new PlayerDecisionEvent<>(1, PlayCardsBeforeSkillCheckEvent.class),
                        // engine can add here player decision for TiebreakCardsBeforeSkillCheckEvent
                        new PlayerDecisionEvent<>(2, PlayCardsIntoSkillCheckEvent.class),
                        new PlayerDecisionEvent<>(3, PlayCardsIntoSkillCheckEvent.class),
                        new PlayerDecisionEvent<>(1, PlayCardsIntoSkillCheckEvent.class),
                        new ShuffleAndDivideCardsEvent(),
                        // engine can add here skill check abilities if any
                        new ResolveSkillCheckEvent()
                ),
                FollowupUtils.skillCheckFollowup(game));
    }
}