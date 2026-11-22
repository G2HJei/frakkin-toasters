package xyz.zlatanov.frakkintoasters.event;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.POLITICAL_PROWESS;
import xyz.zlatanov.frakkintoasters.event.skillcheck.OfferPlayOrPassEvent;
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

    // "Political Prowess": "Play before making a skill check triggered by a location." Each player is asked as the
    // check stands when it is his turn to answer.
    @Test
    void anyPlayerMayPlayPoliticalProwessBeforeALocationSkillCheck() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
        assertEquals(
                all(
                        new OfferPlayOrPassEvent(1, POLITICAL_PROWESS),
                        new OfferPlayOrPassEvent(2, POLITICAL_PROWESS),
                        new OfferPlayOrPassEvent(3, POLITICAL_PROWESS),
                        new MakeSkillCheckEvent()),
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
