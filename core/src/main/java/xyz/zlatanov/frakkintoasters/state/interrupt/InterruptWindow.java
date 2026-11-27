package xyz.zlatanov.frakkintoasters.state.interrupt;

import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

/**
 * A player may use a character ability there too ({@code RollAbilities}). Players who want to play at the same time
 * answer one at a time, and the current player settles conflicts (rule 13.3).
 */
public sealed interface InterruptWindow {

    Set<SkillCardType> cards();

    default boolean startsDieRoll() {
        return false;
    }

    default boolean followsDieRoll() {
        return false;
    }

    // "Strategic Planning": "Play before any die roll to add 2 to the result."
    record BeforeDieRoll() implements InterruptWindow {

        @Override
        public Set<SkillCardType> cards() {
            return Set.of(STRATEGIC_PLANNING);
        }

        @Override
        public boolean startsDieRoll() {
            return true;
        }
    }

    // The roll of a piloted viper's attack. "Best of the Best": "Play before a piloted viper makes an attack. Instead of
    // rolling the attack, roll a die and destroy that many raiders in the viper's space area." It is a die roll too,
    // so "Strategic Planning" can be played on it.
    record BeforePilotedViperAttack() implements InterruptWindow {

        @Override
        public Set<SkillCardType> cards() {
            return Set.of(STRATEGIC_PLANNING, BEST_OF_THE_BEST);
        }

        @Override
        public boolean startsDieRoll() {
            return true;
        }
    }

    // "Calculations": "Play after a die is rolled to add or subtract 1 from the result."
    record AfterDieRoll() implements InterruptWindow {

        @Override
        public Set<SkillCardType> cards() {
            return Set.of(CALCULATIONS);
        }

        @Override
        public boolean followsDieRoll() {
            return true;
        }
    }

    // The roll of the "FTL Control" action, which Felix Gaeta's "FTL Operator" may reroll. Otherwise an AfterDieRoll.
    record AfterFtlControlRoll() implements InterruptWindow {

        @Override
        public Set<SkillCardType> cards() {
            return Set.of(CALCULATIONS);
        }

        @Override
        public boolean followsDieRoll() {
            return true;
        }
    }

    // The roll of an attack on a viper. "Evasive Maneuvers": "Play after any viper is attacked to reroll the die. If the
    // viper is piloted, subtract 2 from the new roll."
    record AfterViperAttacked(int humanFighterId) implements InterruptWindow {

        @Override
        public Set<SkillCardType> cards() {
            return Set.of(CALCULATIONS, EVASIVE_MANOEUVRES);
        }

        @Override
        public boolean followsDieRoll() {
            return true;
        }
    }

    // "Run Interference": "Play before raiders are activated. The first 4 raiders activating in your space area
    // automatically miss (do not roll for them)."
    record BeforeRaidersActivate() implements InterruptWindow {

        @Override
        public Set<SkillCardType> cards() {
            return Set.of(RUN_INTERFERENCE);
        }
    }

    // "Major Victory": "Play after you destroy a basestar or a centurion on the Boarding Party track. Roll a die. If 5
    // or higher, gain 1 morale. Limit of 1 "Major Victory" card used per turn."
    record AfterBasestarOrCenturionDestroyed() implements InterruptWindow {

        @Override
        public Set<SkillCardType> cards() {
            return Set.of(MAJOR_VICTORY);
        }
    }
}
