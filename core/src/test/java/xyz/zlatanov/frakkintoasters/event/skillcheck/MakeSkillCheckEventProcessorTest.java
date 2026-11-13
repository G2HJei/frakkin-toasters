package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class MakeSkillCheckEventProcessorTest extends EventTestHarness<MakeSkillCheckEvent> {

    @Test
    void shouldLetPlayersPlayCardsBeforeTheCheck() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
        execute(new MakeSkillCheckEvent());
        assertFollowup(FollowupUtils.playCardsBeforeSkillCheck(game));
    }

    // "Political Prowess": "Do not make the skill check; instead, it automatically passes or fails".
    @Test
    void shouldResolveAPassWithoutMakingTheCheck() {
        game.startSkillCheck(ADMIRALS_QUARTERS).result(PASS);

        execute(new MakeSkillCheckEvent());

        assertFollowup(all(
                one(new PlayerDecisionEvent<>(1, ChangeOfPlansEvent.class), new NoOpEvent(1)),
                one(new PlayerDecisionEvent<>(2, ChangeOfPlansEvent.class), new NoOpEvent(2)),
                one(new PlayerDecisionEvent<>(3, ChangeOfPlansEvent.class), new NoOpEvent(3)),
                single(new ResolveSkillCheckResultEvent()),
                single(new EndSkillCheckEvent())));
    }

    @Test
    void shouldResolveAFailWithoutMakingTheCheck() {
        game.startSkillCheck(ADMIRALS_QUARTERS).result(FAIL);

        execute(new MakeSkillCheckEvent());

        assertFollowup(all(new ResolveSkillCheckResultEvent(), new EndSkillCheckEvent()));
    }

    @Test
    void shouldNotAllowWithoutASkillCheck() {
        assertInvalid(new MakeSkillCheckEvent());
    }
}
