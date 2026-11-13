package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.MakeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsBeforeSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PoliticalProwessEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.RevealCardsBeforeSkillCheckEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.POWER_FAILURE;

class FollowupUtilsTest extends TestHarness {

    @Test
    void aCrisisSkillCheckIsMadeRightAway() {
        game.startSkillCheck(POWER_FAILURE);
        assertEquals(single(new MakeSkillCheckEvent()), FollowupUtils.beforeSkillCheckFollowups(game));
    }

    // "Political Prowess": "Play before making a skill check triggered by a location."
    @Test
    void anyPlayerMayPlayPoliticalProwessBeforeALocationSkillCheck() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
        assertEquals(
                all(
                        one(new PlayerDecisionEvent<>(1, PoliticalProwessEvent.class), new NoOpEvent(1)),
                        one(new PlayerDecisionEvent<>(2, PoliticalProwessEvent.class), new NoOpEvent(2)),
                        one(new PlayerDecisionEvent<>(3, PoliticalProwessEvent.class), new NoOpEvent(3)),
                        single(new MakeSkillCheckEvent())),
                FollowupUtils.beforeSkillCheckFollowups(game));
    }

    @Test
    void playersPlayCardsBeforeTheCheckFromTheLeftOfTheCurrentPlayer() {
        assertEquals(
                all(
                        all(new PlayerDecisionEvent<>(2, PlayCardsBeforeSkillCheckEvent.class),
                                new PlayerDecisionEvent<>(3, PlayCardsBeforeSkillCheckEvent.class),
                                new PlayerDecisionEvent<>(1, PlayCardsBeforeSkillCheckEvent.class)),
                        single(new RevealCardsBeforeSkillCheckEvent())),
                FollowupUtils.playCardsBeforeSkillCheck(game));
    }
}
