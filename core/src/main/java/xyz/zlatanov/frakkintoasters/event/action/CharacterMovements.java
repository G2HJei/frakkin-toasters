package xyz.zlatanov.frakkintoasters.event.action;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.event.action.character.*;
import xyz.zlatanov.frakkintoasters.event.character.CharacterAbilities;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.state.character.AbilityKind.MOVEMENT;

/**
 * Which "Movement:" abilities of his character sheet a player can use now, instead of moving (rule 8.3.5), in the
 * "Brig" too (rule 10.4.1). A Cylon Leader has them whether he is Infiltrating or not (rule 6.1.3.2), a revealed
 * Cylon's are ignored (rule 6.1.2.2). An ability is offered only when it can happen, as {@link CharacterActions} does.
 */
@NoArgsConstructor(access = PRIVATE)
public final class CharacterMovements {

    /**
     * @return whether the player can use the ability now. Each ability's own rules are in its processor
     */
    public static boolean canUse(GameView game, PlayerView player, CharacterAbility ability) {
        if (ability == null
                || ability.kind() != MOVEMENT
                || !player.hasAbility(ability)
                || ability.isMiracle() && !CharacterAbilities.canUseMiracle(game, player, ability)) {
            return false;
        }
        return switch (ability) {
            case INTIMATE -> IntimateEventProcessor.canUse(game, player);
            case AGGRESSIVE_TACTICS -> AggressiveTacticsEventProcessor.canUse(game, player);
            case GLIMPSE_THE_FACE_OF_GOD -> GlimpseTheFaceOfGodEventProcessor.canUse(game, player);
            case INDUSTRIOUS, METICULOUS, VANITY, EFFICIENT, FAST_LEARNER, EMOTIONALLY_FRAGILE, MEMENTO, ESCORT,
                 FORCED_TO_EJECT, QUICK_FIX, DISCHARGE_OF_A_FIREARM, IMPULSIVE, HUMAN_DELUSION, CONFLICTED_LOYALTIES,
                 PRIMACY, LIES_AND_SECRETS, MAINTENANCE_ENGINEER, BLIND_DEVOTION, RECKLESS, VISIONS, DONT_TRUST_ANYONE,
                 HERETIC, POLITICALLY_ADROIT, MANIPULATIVE, NOTHING_BUT_TROUBLE, FTL_OPERATOR, COUP, MISGUIDED,
                 DELUSIONAL_INTUITION, CYLON_DETECTOR, COWARD, CULT_LEADER, BROADCAST, SPIRITUAL_CRISIS, INTOLERANT,
                 BLIND_JUMP, BENT_ON_REVENGE, ECO_OFFICER, MORAL_COMPASS, STRANDED, RAPTOR_PILOT,
                 NO_ONE_GETS_LEFT_BEHIND, FAMILY_COMMITMENTS, EXPERT_PILOT, SECRET_DESTINY, INSUBORDINATE,
                 RELIGIOUS_VISIONS, SKILLED_POLITICIAN, TERMINAL_ILLNESS, FORWARD_THINKER, CHOOSE_A_DIFFERENT_PATH,
                 MORAL_DILEMMA, ALERT_VIPER_PILOT, CAG, HEADSTRONG, CRYPTIC_MESSAGE, CLOUDED, HOTSHOT, SACRIFICE,
                 STIM_JUNKIE, DUTIFUL, ORGANIZED, RELUCTANT, DECEITFUL, ATTORNEY, KLEPTOMANIA, STAR_PLAYER, LONGSHOT,
                 STARTS_ON_THE_BENCH, CYLON_HATRED, DECLARE_MARTIAL_LAW, ALCOHOLIC, FOR_LOVE, RESOLUTE, GRIEVING,
                 RECON, MYSTERIOUS_INTUITION, SLEEPER_AGENT, TREATMENT, QUARANTINE, SPECIALIZED, CALCULATING,
                 MODIFICATIONS, LOGIC_BOUND, FRIENDS_IN_LOW_PLACES, UNCONVENTIONAL_TACTICS, CONVICTED_CRIMINAL,
                 NECESSARY_STEPS, ABUSE_POWER, DISREPUTABLE, ADAPTABLE, INFLUENTIAL, AMORAL, INSPIRATIONAL_LEADER,
                 COMMAND_AUTHORITY, EMOTIONALLY_ATTACHED -> false;
        };
    }

    /**
     * @return whether the player can use a "Movement:" ability of his sheet now
     */
    public static boolean canUseAny(GameView game, PlayerView player) {
        return player.character() != null
                && CharacterAbility.of(player.character()).stream().anyMatch(ability -> canUse(game, player, ability));
    }

    /**
     * @return the event that uses the ability, {@code null} for an ability that isn't a "Movement:"
     */
    public static Class<? extends CharacterMovementEvent> eventClass(CharacterAbility ability) {
        return switch (ability) {
            case INTIMATE -> IntimateEvent.class;
            case AGGRESSIVE_TACTICS -> AggressiveTacticsEvent.class;
            case GLIMPSE_THE_FACE_OF_GOD -> GlimpseTheFaceOfGodEvent.class;
            case INDUSTRIOUS, METICULOUS, VANITY, EFFICIENT, FAST_LEARNER, EMOTIONALLY_FRAGILE, MEMENTO, ESCORT,
                 FORCED_TO_EJECT, QUICK_FIX, DISCHARGE_OF_A_FIREARM, IMPULSIVE, HUMAN_DELUSION, CONFLICTED_LOYALTIES,
                 PRIMACY, LIES_AND_SECRETS, MAINTENANCE_ENGINEER, BLIND_DEVOTION, RECKLESS, VISIONS, DONT_TRUST_ANYONE,
                 HERETIC, POLITICALLY_ADROIT, MANIPULATIVE, NOTHING_BUT_TROUBLE, FTL_OPERATOR, COUP, MISGUIDED,
                 DELUSIONAL_INTUITION, CYLON_DETECTOR, COWARD, CULT_LEADER, BROADCAST, SPIRITUAL_CRISIS, INTOLERANT,
                 BLIND_JUMP, BENT_ON_REVENGE, ECO_OFFICER, MORAL_COMPASS, STRANDED, RAPTOR_PILOT,
                 NO_ONE_GETS_LEFT_BEHIND, FAMILY_COMMITMENTS, EXPERT_PILOT, SECRET_DESTINY, INSUBORDINATE,
                 RELIGIOUS_VISIONS, SKILLED_POLITICIAN, TERMINAL_ILLNESS, FORWARD_THINKER, CHOOSE_A_DIFFERENT_PATH,
                 MORAL_DILEMMA, ALERT_VIPER_PILOT, CAG, HEADSTRONG, CRYPTIC_MESSAGE, CLOUDED, HOTSHOT, SACRIFICE,
                 STIM_JUNKIE, DUTIFUL, ORGANIZED, RELUCTANT, DECEITFUL, ATTORNEY, KLEPTOMANIA, STAR_PLAYER, LONGSHOT,
                 STARTS_ON_THE_BENCH, CYLON_HATRED, DECLARE_MARTIAL_LAW, ALCOHOLIC, FOR_LOVE, RESOLUTE, GRIEVING,
                 RECON, MYSTERIOUS_INTUITION, SLEEPER_AGENT, TREATMENT, QUARANTINE, SPECIALIZED, CALCULATING,
                 MODIFICATIONS, LOGIC_BOUND, FRIENDS_IN_LOW_PLACES, UNCONVENTIONAL_TACTICS, CONVICTED_CRIMINAL,
                 NECESSARY_STEPS, ABUSE_POWER, DISREPUTABLE, ADAPTABLE, INFLUENTIAL, AMORAL, INSPIRATIONAL_LEADER,
                 COMMAND_AUTHORITY, EMOTIONALLY_ATTACHED -> null;
        };
    }
}
