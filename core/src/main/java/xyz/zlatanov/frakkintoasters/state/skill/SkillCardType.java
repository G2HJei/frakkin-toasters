package xyz.zlatanov.frakkintoasters.state.skill;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Set;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum SkillCardType {
    CONSOLIDATE_POWER(POLITICS),
    INVESTIGATIVE_COMMITTEE(POLITICS),
    SUPPORT_THE_PEOPLE(POLITICS),
    PREVENTIVE_POLICY(POLITICS),
    RED_TAPE(POLITICS),
    POLITICAL_PROWESS(POLITICS),
    FORCE_THEIR_HAND(POLITICS),
    POPULAR_INFLUENCE(POLITICS),
    NEGOTIATION(POLITICS),

    EXECUTIVE_ORDER(LEADERSHIP),
    DECLARE_EMERGENCY(LEADERSHIP),
    MAJOR_VICTORY(LEADERSHIP),
    AT_ANY_COST(LEADERSHIP),
    IRON_WILL(LEADERSHIP),
    STATE_OF_EMERGENCY(LEADERSHIP),
    ALL_HANDS_ON_DECK(LEADERSHIP),
    RESTORE_ORDER(LEADERSHIP),
    CHANGE_OF_PLANS(LEADERSHIP),

    LAUNCH_SCOUT(TACTICS),
    STRATEGIC_PLANNING(TACTICS),
    GUTS_AND_INITIATIVE(TACTICS),
    CRITICAL_SITUATION(TACTICS),
    TRUST_INSTINCTS(TACTICS),
    SCOUTING_FOR_FUEL(TACTICS),
    QUICK_THINKING(TACTICS),
    UNORTHODOX_PLAN(TACTICS),
    A_SECOND_CHANCE(TACTICS),

    EVASIVE_MANOEUVRES(PILOTING),
    MAXIMUM_FIREPOWER(PILOTING),
    FULL_THROTTLE(PILOTING),
    RUN_INTERFERENCE(PILOTING),
    PROTECT_THE_FLEET(PILOTING),
    BEST_OF_THE_BEST(PILOTING),
    DOGFIGHT(PILOTING),
    COMBAT_VETERAN(PILOTING),
    LAUNCH_RESERVES(PILOTING),

    REPAIR(ENGINEERING),
    SCIENTIFIC_RESEARCH(ENGINEERING),
    JURY_RIGGED(ENGINEERING),
    CALCULATIONS(ENGINEERING),
    ESTABLISH_NETWORK(ENGINEERING),
    BUILD_NUKE(ENGINEERING),
    INSTALL_UPGRADES(ENGINEERING),
    RAPTOR_SPECIALIST(ENGINEERING),
    TEST_THE_LIMITS(ENGINEERING),

    DRADIS_CONTACT(TREACHERY),
    BAIT(TREACHERY),
    A_BETTER_MACHINE(TREACHERY),
    PERSONAL_VICES(TREACHERY),
    VIOLENT_OUTBURSTS(TREACHERY),
    EXPLOIT_A_WEAKNESS(TREACHERY);

    private final SkillCardColor color;

    private static final Set<SkillCardType> BEFORE_SKILL_CHECK_CARDS = Set.of(
            INVESTIGATIVE_COMMITTEE, SCIENTIFIC_RESEARCH, SUPPORT_THE_PEOPLE, AT_ANY_COST, GUTS_AND_INITIATIVE, JURY_RIGGED, RESTORE_ORDER, A_SECOND_CHANCE);

    private static final Set<SkillCardType> SKILL_CHECK_ABILITY_CARDS = Set.of(
            DOGFIGHT, FORCE_THEIR_HAND, QUICK_THINKING, A_BETTER_MACHINE, BAIT, DRADIS_CONTACT, EXPLOIT_A_WEAKNESS,
            PERSONAL_VICES, VIOLENT_OUTBURSTS, PROTECT_THE_FLEET, RED_TAPE, TRUST_INSTINCTS, INSTALL_UPGRADES,
            ALL_HANDS_ON_DECK, ESTABLISH_NETWORK, IRON_WILL);

    private static final Set<SkillCardType> RECKLESS_CARDS = Set.of(SUPPORT_THE_PEOPLE, AT_ANY_COST, GUTS_AND_INITIATIVE, JURY_RIGGED);

    // "Action:" cards, played as the player's action (rule 4.2). "Full Throttle" is "Movement or Action:".
    private static final Set<SkillCardType> ACTION_CARDS = Set.of(
            CONSOLIDATE_POWER, POPULAR_INFLUENCE, NEGOTIATION, EXECUTIVE_ORDER, STATE_OF_EMERGENCY, LAUNCH_SCOUT,
            SCOUTING_FOR_FUEL, UNORTHODOX_PLAN, MAXIMUM_FIREPOWER, FULL_THROTTLE, COMBAT_VETERAN, LAUNCH_RESERVES, REPAIR,
            BUILD_NUKE, RAPTOR_SPECIALIST, TEST_THE_LIMITS);

    // "Movement:" cards, used instead of moving in the player's own Movement step (rule 8.3.5).
    private static final Set<SkillCardType> MOVEMENT_CARDS = Set.of(PREVENTIVE_POLICY, CRITICAL_SITUATION, FULL_THROTTLE);

    public boolean isAction() {
        return ACTION_CARDS.contains(this);
    }

    public boolean isMovementAbility() {
        return MOVEMENT_CARDS.contains(this);
    }

    public boolean hasSkillCheckAbility() {
        return SKILL_CHECK_ABILITY_CARDS.contains(this);
    }

    public boolean playableBeforeSkillCheck() {
        return BEFORE_SKILL_CHECK_CARDS.contains(this);
    }

    public boolean reckless() {
        return RECKLESS_CARDS.contains(this);
    }

    public boolean recklessOrRestoreOrder() {
        return reckless() || this == RESTORE_ORDER;
    }
}
