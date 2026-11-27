package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.location.galactica.CommunicationsEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.WeaponsControlEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.ANYWHERE_EVEN_IF_DAMAGED;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.UNORTHODOX_PLAN;

class UnorthodoxPlanEventProcessorTest extends EventTestHarness<UnorthodoxPlanEvent> {

    private static final SkillCard UNORTHODOX_PLAN_3 = new SkillCard(3, UNORTHODOX_PLAN);

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        player(1).gainSkillCards(UNORTHODOX_PLAN_3);
    }

    // From elsewhere, and even when it is damaged.
    @Test
    void weaponsControlFromElsewhereWhileDamaged() {
        raiderAt(GALACTICA_SPACE_9_OCLOCK);
        ops.sandbox().damage(WEAPONS_CONTROL);

        execute(new UnorthodoxPlanEvent(1, UNORTHODOX_PLAN_3, WEAPONS_CONTROL));

        assertFollowup(new PlayerDecisionEvent<>(1, WeaponsControlEvent.class, ANYWHERE_EVEN_IF_DAMAGED));
    }

    @Test
    void communicationsWithACivilianShipInSpace() {
        civilianShipAt(GALACTICA_SPACE_3_OCLOCK);

        execute(new UnorthodoxPlanEvent(1, UNORTHODOX_PLAN_3, COMMUNICATIONS));

        assertFollowup(new PlayerDecisionEvent<>(1, CommunicationsEvent.class, ANYWHERE_EVEN_IF_DAMAGED));
    }

    @Test
    void theArmoryNeedsACenturion() {
        assertInvalid(new UnorthodoxPlanEvent(1, UNORTHODOX_PLAN_3, ARMORY));
    }

    @Test
    void communicationsNeedsACivilianShip() {
        assertInvalid(new UnorthodoxPlanEvent(1, UNORTHODOX_PLAN_3, COMMUNICATIONS));
    }

    @Test
    void onlyItsFourLocations() {
        assertInvalid(new UnorthodoxPlanEvent(1, UNORTHODOX_PLAN_3, FTL_CONTROL));
    }

    @Test
    void theLocationIsNamed() {
        assertInvalid(new UnorthodoxPlanEvent(1, UNORTHODOX_PLAN_3, null));
    }

    // "Terminal Illness" costs Laura Roslin 2 cards before the action, from what is left once the card is played
    // (F-130933).
    @Test
    void notWithoutTheCardsTheLocationCosts() {
        player(2).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        player(2).gainSkillCards(UNORTHODOX_PLAN_3, new SkillCard(1, CONSOLIDATE_POWER));
        raiderAt(GALACTICA_SPACE_3_OCLOCK);

        assertFalse(UnorthodoxPlanEventProcessor.canPlay(game, player(2)));
        assertInvalid(new UnorthodoxPlanEvent(2, UNORTHODOX_PLAN_3, WEAPONS_CONTROL));
    }
}
