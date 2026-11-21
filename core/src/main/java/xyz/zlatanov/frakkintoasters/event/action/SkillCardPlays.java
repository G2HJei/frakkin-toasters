package xyz.zlatanov.frakkintoasters.event.action;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.BuildNukeEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.ConsolidatePowerEventProcessor;
import xyz.zlatanov.frakkintoasters.event.action.skillcard.CriticalSituationEventProcessor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

/**
 * Which Skill Cards a player can play for their "Action:" or "Movement:" now. A card is offered only when its effect can
 * happen, as a location's action is ({@code LocationEvent.canAct}), so a decision to play one always has an answer.
 */
@NoArgsConstructor(access = PRIVATE)
public final class SkillCardPlays {

    /**
     * @return whether the player could play a card of the type now, if he held one. Each card's own rules are in its
     * processor
     */
    public static boolean canPlay(GameView game, PlayerView player, SkillCardType type) {
        // Revealed Cylons, and Cylon Leaders who aren't Infiltrating, can't use them (rules 6.1.2.3, 6.2.1).
        if (!player.isHuman()) {
            return false;
        }
        return switch (type) {
            case CONSOLIDATE_POWER -> ConsolidatePowerEventProcessor.canPlay(game);
            case BUILD_NUKE -> BuildNukeEventProcessor.canPlay(game);
            case CRITICAL_SITUATION -> CriticalSituationEventProcessor.canPlay(game, player);
            case PREVENTIVE_POLICY -> true;
            // not built yet
            case POPULAR_INFLUENCE, NEGOTIATION, EXECUTIVE_ORDER, STATE_OF_EMERGENCY, LAUNCH_SCOUT,
                 SCOUTING_FOR_FUEL, UNORTHODOX_PLAN, MAXIMUM_FIREPOWER, FULL_THROTTLE, COMBAT_VETERAN,
                 LAUNCH_RESERVES, REPAIR, RAPTOR_SPECIALIST, TEST_THE_LIMITS -> false;
            // played in a skill check, before it, or in an interrupt window, never as an action or a movement
            case INVESTIGATIVE_COMMITTEE, SUPPORT_THE_PEOPLE, RED_TAPE, POLITICAL_PROWESS, FORCE_THEIR_HAND,
                 DECLARE_EMERGENCY, MAJOR_VICTORY, AT_ANY_COST, IRON_WILL, ALL_HANDS_ON_DECK, RESTORE_ORDER,
                 CHANGE_OF_PLANS, STRATEGIC_PLANNING, GUTS_AND_INITIATIVE, TRUST_INSTINCTS, QUICK_THINKING,
                 A_SECOND_CHANCE, EVASIVE_MANOEUVRES, RUN_INTERFERENCE, PROTECT_THE_FLEET, BEST_OF_THE_BEST, DOGFIGHT,
                 SCIENTIFIC_RESEARCH, JURY_RIGGED, CALCULATIONS, ESTABLISH_NETWORK, INSTALL_UPGRADES, DRADIS_CONTACT,
                 BAIT, A_BETTER_MACHINE, PERSONAL_VICES, VIOLENT_OUTBURSTS, EXPLOIT_A_WEAKNESS -> false;
        };
    }

    /**
     * @return whether the player holds an "Action:" card he can play now
     */
    public static boolean canPlayAction(GameView game, PlayerView player) {
        return held(player).filter(SkillCardType::isAction).anyMatch(type -> canPlay(game, player, type));
    }

    /**
     * @return whether the player holds a "Movement:" card he can play now
     */
    public static boolean canPlayMovementAbility(GameView game, PlayerView player) {
        return held(player).filter(SkillCardType::isMovementAbility).anyMatch(type -> canPlay(game, player, type));
    }

    // Filtered by timing before canPlay: "Critical Situation" asks whether an "Action:" card can be played.
    private static Stream<SkillCardType> held(PlayerView player) {
        return player.skillCards().cards().stream().map(SkillCard::type).distinct();
    }
}
