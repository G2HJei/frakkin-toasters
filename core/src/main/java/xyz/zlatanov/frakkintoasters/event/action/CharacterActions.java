package xyz.zlatanov.frakkintoasters.event.action;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.event.action.character.*;
import xyz.zlatanov.frakkintoasters.event.character.CharacterAbilities;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.state.character.AbilityKind.ACTION;

/**
 * Which "Action:" abilities of his character sheet a player can use now. An ability is offered only when it can
 * happen, as a Skill Card is ({@link SkillCardPlays}), so a decision to use one always has an answer. A Cylon Leader
 * uses them instead of his location's action (rule 6.2.1.2), a revealed Cylon's are ignored (rule 6.1.2.2).
 */
@NoArgsConstructor(access = PRIVATE)
public final class CharacterActions {

    /**
     * @return whether the ability is an action of the player's sheet that is in effect, with the miracle tokens it spends
     */
    public static boolean isAvailable(GameView game, PlayerView player, CharacterAbility ability) {
        return ability != null
                && ability.kind() == ACTION
                && player.hasAbility(ability)
                && (!ability.isMiracle() || CharacterAbilities.canUseMiracle(game, player, ability));
    }

    /**
     * @return whether the player can use the ability now. Each ability's own rules are in its processor
     */
    public static boolean canUse(GameView game, PlayerView player, CharacterAbility ability) {
        if (!isAvailable(game, player, ability)) {
            return false;
        }
        return switch (ability) {
            case PRIMACY -> PrimacyEventProcessor.canUse(game, player);
            case VISIONS -> VisionsEventProcessor.canUse(game, player);
            case DONT_TRUST_ANYONE -> DontTrustAnyoneEventProcessor.canUse(game, player);
            case CULT_LEADER -> CultLeaderEventProcessor.canUse(game, player);
            case BROADCAST -> BroadcastEventProcessor.canUse(game, player);
            case STAR_PLAYER -> StarPlayerEventProcessor.canUse(game, player);
            case TREATMENT -> TreatmentEventProcessor.canUse(game, player);
            case DISCHARGE_OF_A_FIREARM -> DischargeOfAFirearmEventProcessor.canUse(game, player);
            case COUP -> CoupEventProcessor.canUse(game, player);
            case CYLON_DETECTOR -> CylonDetectorEventProcessor.canUse(game, player);
            case BLIND_JUMP -> BlindJumpEventProcessor.canUse(game, player);
            case SKILLED_POLITICIAN -> SkilledPoliticianEventProcessor.canUse(game, player);
            case CAG -> CagAbilityEventProcessor.canUse(game, player);
            case CRYPTIC_MESSAGE -> CrypticMessageEventProcessor.canUse(game, player);
            case SACRIFICE -> SacrificeEventProcessor.canUse(game, player);
            case ORGANIZED -> OrganizedEventProcessor.canUse(game, player);
            case ATTORNEY -> AttorneyEventProcessor.canUse(game, player);
            case DECLARE_MARTIAL_LAW -> DeclareMartialLawEventProcessor.canUse(game, player);
            case RESOLUTE -> ResoluteEventProcessor.canUse(game, player);
            case QUARANTINE -> QuarantineEventProcessor.canUse(game, player);
            case UNCONVENTIONAL_TACTICS -> UnconventionalTacticsEventProcessor.canUse(game, player);
            case ABUSE_POWER -> AbusePowerEventProcessor.canUse(game, player);
            case INFLUENTIAL -> InfluentialEventProcessor.canUse(game, player);
            case INDUSTRIOUS, METICULOUS, VANITY, EFFICIENT, FAST_LEARNER, EMOTIONALLY_FRAGILE, MEMENTO, ESCORT,
                 FORCED_TO_EJECT, QUICK_FIX, IMPULSIVE, INTIMATE, HUMAN_DELUSION, CONFLICTED_LOYALTIES,
                 AGGRESSIVE_TACTICS, LIES_AND_SECRETS, MAINTENANCE_ENGINEER, BLIND_DEVOTION, RECKLESS, HERETIC,
                 POLITICALLY_ADROIT, MANIPULATIVE, NOTHING_BUT_TROUBLE, FTL_OPERATOR, MISGUIDED,
                 DELUSIONAL_INTUITION, COWARD, SPIRITUAL_CRISIS, INTOLERANT, BENT_ON_REVENGE, ECO_OFFICER,
                 MORAL_COMPASS, STRANDED, RAPTOR_PILOT, NO_ONE_GETS_LEFT_BEHIND, FAMILY_COMMITMENTS, EXPERT_PILOT,
                 SECRET_DESTINY, INSUBORDINATE, RELIGIOUS_VISIONS, TERMINAL_ILLNESS, FORWARD_THINKER,
                 CHOOSE_A_DIFFERENT_PATH, MORAL_DILEMMA, ALERT_VIPER_PILOT, HEADSTRONG, GLIMPSE_THE_FACE_OF_GOD,
                 CLOUDED, HOTSHOT, STIM_JUNKIE, DUTIFUL, RELUCTANT, DECEITFUL, KLEPTOMANIA, LONGSHOT,
                 STARTS_ON_THE_BENCH, CYLON_HATRED, ALCOHOLIC, FOR_LOVE, GRIEVING, RECON, MYSTERIOUS_INTUITION,
                 SLEEPER_AGENT, SPECIALIZED, CALCULATING, MODIFICATIONS, LOGIC_BOUND, FRIENDS_IN_LOW_PLACES,
                 CONVICTED_CRIMINAL, NECESSARY_STEPS, DISREPUTABLE, ADAPTABLE, AMORAL, INSPIRATIONAL_LEADER,
                 COMMAND_AUTHORITY, EMOTIONALLY_ATTACHED -> false;
        };
    }

    /**
     * @return whether the player can use an action of his sheet now
     */
    public static boolean canUseAny(GameView game, PlayerView player) {
        return player.character() != null
                && CharacterAbility.of(player.character()).stream().anyMatch(ability -> canUse(game, player, ability));
    }

    /**
     * @return the event that uses the ability, {@code null} for an ability that isn't an action
     */
    public static Class<? extends CharacterActionEvent> eventClass(CharacterAbility ability) {
        return switch (ability) {
            case PRIMACY -> PrimacyEvent.class;
            case VISIONS -> VisionsEvent.class;
            case DONT_TRUST_ANYONE -> DontTrustAnyoneEvent.class;
            case CULT_LEADER -> CultLeaderEvent.class;
            case BROADCAST -> BroadcastEvent.class;
            case STAR_PLAYER -> StarPlayerEvent.class;
            case TREATMENT -> TreatmentEvent.class;
            case DISCHARGE_OF_A_FIREARM -> DischargeOfAFirearmEvent.class;
            case COUP -> CoupEvent.class;
            case CYLON_DETECTOR -> CylonDetectorEvent.class;
            case BLIND_JUMP -> BlindJumpEvent.class;
            case SKILLED_POLITICIAN -> SkilledPoliticianEvent.class;
            case CAG -> CagAbilityEvent.class;
            case CRYPTIC_MESSAGE -> CrypticMessageEvent.class;
            case SACRIFICE -> SacrificeEvent.class;
            case ORGANIZED -> OrganizedEvent.class;
            case ATTORNEY -> AttorneyEvent.class;
            case DECLARE_MARTIAL_LAW -> DeclareMartialLawEvent.class;
            case RESOLUTE -> ResoluteEvent.class;
            case QUARANTINE -> QuarantineEvent.class;
            case UNCONVENTIONAL_TACTICS -> UnconventionalTacticsEvent.class;
            case ABUSE_POWER -> AbusePowerEvent.class;
            case INFLUENTIAL -> InfluentialEvent.class;
            case INDUSTRIOUS, METICULOUS, VANITY, EFFICIENT, FAST_LEARNER, EMOTIONALLY_FRAGILE, MEMENTO, ESCORT,
                 FORCED_TO_EJECT, QUICK_FIX, IMPULSIVE, INTIMATE, HUMAN_DELUSION, CONFLICTED_LOYALTIES,
                 AGGRESSIVE_TACTICS, LIES_AND_SECRETS, MAINTENANCE_ENGINEER, BLIND_DEVOTION, RECKLESS, HERETIC,
                 POLITICALLY_ADROIT, MANIPULATIVE, NOTHING_BUT_TROUBLE, FTL_OPERATOR, MISGUIDED,
                 DELUSIONAL_INTUITION, COWARD, SPIRITUAL_CRISIS, INTOLERANT, BENT_ON_REVENGE, ECO_OFFICER,
                 MORAL_COMPASS, STRANDED, RAPTOR_PILOT, NO_ONE_GETS_LEFT_BEHIND, FAMILY_COMMITMENTS, EXPERT_PILOT,
                 SECRET_DESTINY, INSUBORDINATE, RELIGIOUS_VISIONS, TERMINAL_ILLNESS, FORWARD_THINKER,
                 CHOOSE_A_DIFFERENT_PATH, MORAL_DILEMMA, ALERT_VIPER_PILOT, HEADSTRONG, GLIMPSE_THE_FACE_OF_GOD,
                 CLOUDED, HOTSHOT, STIM_JUNKIE, DUTIFUL, RELUCTANT, DECEITFUL, KLEPTOMANIA, LONGSHOT,
                 STARTS_ON_THE_BENCH, CYLON_HATRED, ALCOHOLIC, FOR_LOVE, GRIEVING, RECON, MYSTERIOUS_INTUITION,
                 SLEEPER_AGENT, SPECIALIZED, CALCULATING, MODIFICATIONS, LOGIC_BOUND, FRIENDS_IN_LOW_PLACES,
                 CONVICTED_CRIMINAL, NECESSARY_STEPS, DISREPUTABLE, ADAPTABLE, AMORAL, INSPIRATIONAL_LEADER,
                 COMMAND_AUTHORITY, EMOTIONALLY_ATTACHED -> null;
        };
    }
}
