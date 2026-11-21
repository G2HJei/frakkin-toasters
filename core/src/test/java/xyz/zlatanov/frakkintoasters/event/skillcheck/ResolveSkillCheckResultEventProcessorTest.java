package xyz.zlatanov.frakkintoasters.event.skillcheck;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.HELENA_CAIN;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;
import xyz.zlatanov.frakkintoasters.event.character.IntolerantEvent;
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

    // "Intolerant": on a pass of 10 or more, Helena Cain may have the target executed instead of sent to the "Brig".
    @Test
    void intolerantOnAStrongAdmiralsQuartersPass() {
        intolerantCheck(10);

        execute(new ResolveSkillCheckResultEvent());

        assertFollowup(one(new IntolerantEvent(1, 2), new SendToBrigEvent(2)));
    }

    @Test
    void notIntolerantOnAWeakerPass() {
        intolerantCheck(9);

        execute(new ResolveSkillCheckResultEvent());

        assertFollowup(new SendToBrigEvent(2));
    }

    private void intolerantCheck(int strength) {
        player(1).character(HELENA_CAIN);
        moveTo(Location.COMMAND, HELENA_CAIN);
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(Location.HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.skillCheck().start(ADMIRALS_QUARTERS, 2);
        ops.skillCheck().totalStrength(strength);
        ops.skillCheck().determineResult(PASS);
    }
}
