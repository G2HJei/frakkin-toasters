package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.InstallUpgradesEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.IronWillEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.SkillCheckView;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playOrPassDecisions;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.AIRLOCK;

public class DetermineSkillCheckResultEventProcessor extends EventProcessor<DetermineSkillCheckResultEvent> {

    private static final int DECLARE_EMERGENCY_REDUCTION = 2;
    private static final int JURY_RIGGED_REDUCTION       = 4;
    private static final int AIRLOCK_BRIG_REDUCTION      = 4;
    private static final int IRON_WILL_MARGIN            = 4;
    private static final int A_SECOND_CHANCE_MARGIN      = 4;

    /**
     * @return the difficulty of the active skill check, with what reduces it so far
     */
    public static int difficulty(GameView game) {
        val skillCheck = game.activeSkillCheck();
        // "Declare Emergency" and "Jury Rigged" reduce the difficulty, but never the partial pass number.
        return skillCheck.type().difficulty()
                - (skillCheck.emergencyDeclared() ? DECLARE_EMERGENCY_REDUCTION : 0)
                - (playedBefore(skillCheck, JURY_RIGGED) ? JURY_RIGGED_REDUCTION : 0)
                - (airlockOnPrisoner(game) ? AIRLOCK_BRIG_REDUCTION : 0);
    }

    // Airlock: "Reduce the difficulty of this check by 4 if the character is in the "Brig.""
    private static boolean airlockOnPrisoner(GameView game) {
        val skillCheck = game.activeSkillCheck();
        return skillCheck.type() == AIRLOCK
                && game.locate(game.player(skillCheck.targetPlayerNumber()).character()).filter(BRIG::equals).isPresent();
    }

    @Override
    protected boolean isValid() {
        val skillCheck = game.activeSkillCheck();
        return skillCheck.totalStrength() != null
                && skillCheck.result() == null;
    }

    @Override
    public Followup process() {
        val strength = game.activeSkillCheck().totalStrength();
        val difficulty = difficulty(game);
        val result = result(strength, difficulty);
        ops.skillCheck().determineResult(result);
        if (result == FAIL && abilityResolved(IRON_WILL) && strength >= difficulty - IRON_WILL_MARGIN) {
            ops.skillCheck().preventFailResult();
        }
        return followup(result, strength);
    }

    private SkillCheckResult result(int strength, int difficulty) {
        if (strength >= difficulty) {
            return PASS;
        }
        val partialPass = game.activeSkillCheck().type().partialPass();
        return partialPass != null && strength >= partialPass ? PARTIAL_PASS : FAIL;
    }

    private Followup followup(SkillCheckResult result, int strength) {
        val followups = new ArrayList<Followup>();
        if (abilityResolved(INSTALL_UPGRADES)) {
            followups.add(single(new InstallUpgradesEvent(result == PASS)));
        }
        if (abilityResolved(IRON_WILL) && strength <= 0) {
            followups.add(single(new IronWillEvent()));
        }
        if (result == PASS) {
            followups.addAll(playOrPassDecisions(game, ChangeOfPlansEvent.class));
        }
        // after the Change of Plans decisions, which may replace the pass result
        followups.add(single(new ResolveSkillCheckResultEvent()));
        if (playedBefore(game.activeSkillCheck(), A_SECOND_CHANCE) && passedPrintedDifficultyBy(A_SECOND_CHANCE_MARGIN, strength)) {
            followups.add(single(new ASecondChanceEvent()));
        }
        // after everything the result queued
        followups.add(single(new EndSkillCheckEvent()));
        return all(followups.toArray(Followup[]::new));
    }

    private boolean passedPrintedDifficultyBy(int margin, int strength) {
        return strength >= game.activeSkillCheck().type().difficulty() + margin;
    }

    private boolean abilityResolved(SkillCardType type) {
        return game.activeSkillCheck().resolvedAbilities().contains(type);
    }

    private static boolean playedBefore(SkillCheckView skillCheck, SkillCardType type) {
        return skillCheck.playedBeforeSkillCheck().stream()
                .map(SkillCard::type)
                .anyMatch(type::equals);
    }
}
