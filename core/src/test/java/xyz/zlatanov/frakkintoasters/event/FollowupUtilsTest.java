package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;

class FollowupUtilsTest extends TestHarness {

    @Test
    void shouldBuildAllFollowupEvents() {
        assertEquals(
                all(
                        single(new PlayFromDestinyDeckEvent()),
                        all(
                                one(
                                        new PlayerDecisionEvent<>(2, PlayRestoreOrderEvent.class),
                                        new PlayerDecisionEvent<>(2, PlayRecklessSkillCardEvent.class)),
                                one(
                                        new PlayerDecisionEvent<>(3, PlayRestoreOrderEvent.class),
                                        new PlayerDecisionEvent<>(3, PlayRecklessSkillCardEvent.class)),
                                one(
                                        new PlayerDecisionEvent<>(1, PlayRestoreOrderEvent.class),
                                        new PlayerDecisionEvent<>(1, PlayRecklessSkillCardEvent.class))
                        ),
                        all(
                                one(new PlayerDecisionEvent<>(2, PlaySkillsEvent.class)),
                                one(new PlayerDecisionEvent<>(3, PlaySkillsEvent.class)),
                                one(new PlayerDecisionEvent<>(1, PlaySkillsEvent.class))),
                        single(new ShuffleAndDivideCardsEvent()),
                        single(new ResolveSkillCheckEvent())
                ),
                FollowupUtils.skillCheckFollowup(game));
    }
}