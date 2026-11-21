package xyz.zlatanov.frakkintoasters.event.action.skillcard;

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
        raiderAt(GALACTICA_SPACE_8_OCLOCK);
        ops.sandbox().damage(WEAPONS_CONTROL);

        execute(new UnorthodoxPlanEvent(1, UNORTHODOX_PLAN_3, WEAPONS_CONTROL));

        assertFollowup(new PlayerDecisionEvent<>(1, WeaponsControlEvent.class, ANYWHERE_EVEN_IF_DAMAGED));
    }

    @Test
    void communicationsWithACivilianShipInSpace() {
        civilianShipAt(GALACTICA_SPACE_2_OCLOCK);

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
}
