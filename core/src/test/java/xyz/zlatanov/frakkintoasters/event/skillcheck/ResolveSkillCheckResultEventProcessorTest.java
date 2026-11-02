package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Resource.MORALE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.COLONIAL_DAY;

class ResolveSkillCheckResultEventProcessorTest extends EventTestHarness<ResolveSkillCheckResultEvent> {

    @Test
    void shouldResolvePassResult() {
        game.startSkillCheck(COLONIAL_DAY).result(PASS);

        execute(new ResolveSkillCheckResultEvent());

        assertFollowup(new GainResourceEvent(MORALE, 1));
    }

    @Test
    void shouldResolveFailResult() {
        game.startSkillCheck(COLONIAL_DAY).result(FAIL);

        execute(new ResolveSkillCheckResultEvent());

        assertFollowup(new LoseResourceEvent(MORALE, 2));
    }

    @Test
    void shouldNotResolvePassResultReplacedByChangeOfPlans() {
        game.startSkillCheck(COLONIAL_DAY).result(PASS).replacePassResult();

        execute(new ResolveSkillCheckResultEvent());
    }

    @Test
    void shouldNotResolveFailResultPreventedByIronWill() {
        game.startSkillCheck(COLONIAL_DAY).result(FAIL).preventFailResult();

        execute(new ResolveSkillCheckResultEvent());
    }

    @Test
    void shouldNotAllowBeforeResultIsDetermined() {
        game.startSkillCheck(COLONIAL_DAY);
        assertInvalid(new ResolveSkillCheckResultEvent());
    }
}
