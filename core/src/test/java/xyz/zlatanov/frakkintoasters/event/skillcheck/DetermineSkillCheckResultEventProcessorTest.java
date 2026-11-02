package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.InstallUpgradesEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.IronWillEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.FAIL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class DetermineSkillCheckResultEventProcessorTest extends EventTestHarness<DetermineSkillCheckResultEvent> {

    private SkillCheck skillCheck;

    @BeforeEach
    void setUp() {
        skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS); // difficulty 7
    }

    @Test
    void shouldPassWhenStrengthEqualsDifficulty() {
        skillCheck.totalStrength(7);

        execute(new DetermineSkillCheckResultEvent());

        assertEquals(PASS, skillCheck.result());
        assertFollowup(all(changeOfPlans(1), changeOfPlans(2), changeOfPlans(3), resolve()));
    }

    @Test
    void shouldFailWhenStrengthIsBelowDifficulty() {
        skillCheck.totalStrength(6);

        execute(new DetermineSkillCheckResultEvent());

        assertEquals(FAIL, skillCheck.result());
        assertFalse(skillCheck.failResultPrevented());
        assertFollowup(new ResolveSkillCheckResultEvent());
    }

    @Test
    void shouldReduceDifficultyBy2WhenEmergencyIsDeclared() {
        skillCheck.declareEmergency();
        skillCheck.totalStrength(5);

        execute(new DetermineSkillCheckResultEvent());

        assertEquals(PASS, skillCheck.result());
        assertFollowup(all(changeOfPlans(1), changeOfPlans(2), changeOfPlans(3), resolve()));
    }

    @Test
    void shouldReduceDifficultyBy4WithJuryRigged() {
        skillCheck.playedBefore(1, List.of(new SkillCard(1, JURY_RIGGED)));
        skillCheck.totalStrength(3);

        execute(new DetermineSkillCheckResultEvent());

        assertEquals(PASS, skillCheck.result());
        assertFollowup(all(changeOfPlans(1), changeOfPlans(2), changeOfPlans(3), resolve()));
    }

    @Test
    void shouldCombineDifficultyReductions() {
        skillCheck.playedBefore(1, List.of(new SkillCard(1, JURY_RIGGED)));
        skillCheck.declareEmergency();
        skillCheck.totalStrength(1);

        execute(new DetermineSkillCheckResultEvent());

        assertEquals(PASS, skillCheck.result());
        assertFollowup(all(changeOfPlans(1), changeOfPlans(2), changeOfPlans(3), resolve()));
    }

    @Test
    void shouldPreventFailResultWithIronWillWhenStrengthIsWithin4OfDifficulty() {
        skillCheck.resolved(IRON_WILL);
        skillCheck.totalStrength(3);

        execute(new DetermineSkillCheckResultEvent());

        assertEquals(FAIL, skillCheck.result());
        assertTrue(skillCheck.failResultPrevented());
        assertFollowup(new ResolveSkillCheckResultEvent());
    }

    @Test
    void shouldNotPreventFailResultWithIronWillWhenStrengthIsNotWithin4OfDifficulty() {
        skillCheck.resolved(IRON_WILL);
        skillCheck.totalStrength(2);

        execute(new DetermineSkillCheckResultEvent());

        assertEquals(FAIL, skillCheck.result());
        assertFalse(skillCheck.failResultPrevented());
        assertFollowup(new ResolveSkillCheckResultEvent());
    }

    @Test
    void shouldCompareIronWillWithReducedDifficulty() {
        skillCheck.resolved(IRON_WILL);
        skillCheck.declareEmergency();
        skillCheck.totalStrength(1);

        execute(new DetermineSkillCheckResultEvent());

        assertEquals(FAIL, skillCheck.result());
        assertTrue(skillCheck.failResultPrevented());
        assertFollowup(new ResolveSkillCheckResultEvent());
    }

    @Test
    void shouldLoseMoraleWithIronWillWhenStrengthIs0OrLess() {
        skillCheck.resolved(IRON_WILL);
        skillCheck.totalStrength(0);

        execute(new DetermineSkillCheckResultEvent());

        assertEquals(FAIL, skillCheck.result());
        assertFalse(skillCheck.failResultPrevented());
        assertFollowup(all(new IronWillEvent(), new ResolveSkillCheckResultEvent()));
    }

    @Test
    void shouldDraw2EngineeringCardsWithInstallUpgradesWhenPassed() {
        skillCheck.resolved(INSTALL_UPGRADES);
        skillCheck.totalStrength(7);

        execute(new DetermineSkillCheckResultEvent());

        assertFollowup(all(single(new InstallUpgradesEvent(true)), changeOfPlans(1), changeOfPlans(2), changeOfPlans(3),
                resolve()));
    }

    @Test
    void shouldDraw1EngineeringCardWithInstallUpgradesWhenFailed() {
        skillCheck.resolved(INSTALL_UPGRADES);
        skillCheck.totalStrength(6);

        execute(new DetermineSkillCheckResultEvent());

        assertFollowup(all(new InstallUpgradesEvent(false), new ResolveSkillCheckResultEvent()));
    }

    @Test
    void shouldGainMiracleTokenWithASecondChanceWhenPassingPrintedDifficultyBy4() {
        skillCheck.playedBefore(1, List.of(new SkillCard(5, A_SECOND_CHANCE)));
        skillCheck.totalStrength(11);

        execute(new DetermineSkillCheckResultEvent());

        assertFollowup(all(changeOfPlans(1), changeOfPlans(2), changeOfPlans(3), resolve(),
                single(new ASecondChanceEvent())));
    }

    @Test
    void shouldCompareASecondChanceWithPrintedDifficulty() {
        skillCheck.playedBefore(1, List.of(new SkillCard(5, A_SECOND_CHANCE)));
        skillCheck.declareEmergency();
        skillCheck.totalStrength(9);

        execute(new DetermineSkillCheckResultEvent());

        assertEquals(PASS, skillCheck.result());
        assertFollowup(all(changeOfPlans(1), changeOfPlans(2), changeOfPlans(3), resolve()));
    }

    @Test
    void shouldNotAllowBeforeStrengthIsTotaled() {
        assertInvalid(new DetermineSkillCheckResultEvent());
    }

    @Test
    void shouldNotAllowWhenResultIsAlreadyDetermined() {
        skillCheck.totalStrength(7).result(PASS);
        assertInvalid(new DetermineSkillCheckResultEvent());
    }

    private static Followup changeOfPlans(int playerNumber) {
        return one(new PlayerDecisionEvent<>(playerNumber, ChangeOfPlansEvent.class), new NoOpEvent(playerNumber));
    }

    private static Followup resolve() {
        return single(new ResolveSkillCheckResultEvent());
    }
}
