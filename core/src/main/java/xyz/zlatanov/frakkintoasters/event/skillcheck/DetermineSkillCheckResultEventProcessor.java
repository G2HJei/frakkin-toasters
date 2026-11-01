package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.InstallUpgradesEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.IronWillEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playOrPassDecisions;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.*;

public class DetermineSkillCheckResultEventProcessor extends EventProcessor<DetermineSkillCheckResultEvent> {

    private static final int DECLARE_EMERGENCY_REDUCTION = 2;
    private static final int JURY_RIGGED_REDUCTION       = 4;
    private static final int IRON_WILL_MARGIN            = 4;
    private static final int A_SECOND_CHANCE_MARGIN      = 4;

    @Override
    protected boolean isValid() {
        val skillCheck = game.activeSkillCheck();
        return skillCheck.totalStrength() != null
                && skillCheck.result() == null;
    }

    @Override
    public Followup process() {
        val strength = game.activeSkillCheck().totalStrength();
        val difficulty = difficulty();
        val result = result(strength, difficulty);
        ops.skillCheck().determineResult(result);
        if (result == FAIL && abilityResolved(IRON_WILL) && strength >= difficulty - IRON_WILL_MARGIN) {
            ops.skillCheck().preventFailResult();
        }
        return followup(result, strength);
    }

    // "Declare Emergency" and "Jury Rigged" reduce the difficulty, but never the partial pass number
    private int difficulty() {
        val skillCheck = game.activeSkillCheck();
        return skillCheck.type().difficulty()
                - (skillCheck.emergencyDeclared() ? DECLARE_EMERGENCY_REDUCTION : 0)
                - (playedBefore(JURY_RIGGED) ? JURY_RIGGED_REDUCTION : 0);
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
        //todo resolve the pass, partial pass or fail result, unless replaced by Change of Plans or prevented by Iron Will
        if (playedBefore(A_SECOND_CHANCE) && passedPrintedDifficultyBy(A_SECOND_CHANCE_MARGIN, strength)) {
            followups.add(single(new ASecondChanceEvent()));
        }
        return all(followups.toArray(Followup[]::new));
    }

    private boolean passedPrintedDifficultyBy(int margin, int strength) {
        return strength >= game.activeSkillCheck().type().difficulty() + margin;
    }

    private boolean abilityResolved(SkillCardType type) {
        return game.activeSkillCheck().resolvedAbilities().contains(type);
    }

    private boolean playedBefore(SkillCardType type) {
        return game.activeSkillCheck().playedBeforeSkillCheck().stream()
                .map(SkillCard::type)
                .anyMatch(type::equals);
    }
}
