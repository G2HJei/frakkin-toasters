package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CHANGE_OF_PLANS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.DECLARE_EMERGENCY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class DeclareEmergencyEventProcessorTest extends EventTestHarness<DeclareEmergencyEvent> {

    private static final SkillCard DECLARE_EMERGENCY_3 = new SkillCard(3, DECLARE_EMERGENCY);

    private SkillCheck skillCheck;

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(DECLARE_EMERGENCY_3);
        skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldDeclareEmergency() {
        execute(new DeclareEmergencyEvent(1, DECLARE_EMERGENCY_3));

        assertNoSkillCards(1);
        assertEquals(List.of(DECLARE_EMERGENCY_3), leadershipDeck.discardedCards());
        assertTrue(skillCheck.emergencyDeclared());
    }

    @Test
    void shouldAllowOnlyOnePerSkillCheck() {
        skillCheck.declareEmergency();
        assertInvalid(new DeclareEmergencyEvent(1, DECLARE_EMERGENCY_3));
    }

    @Test
    void shouldNotAllowAfterResultIsDetermined() {
        skillCheck.result(PASS);
        assertInvalid(new DeclareEmergencyEvent(1, DECLARE_EMERGENCY_3));
    }

    @Test
    void shouldNotAllowCardNotInHand() {
        assertInvalid(new DeclareEmergencyEvent(2, DECLARE_EMERGENCY_3));
    }

    @Test
    void shouldNotAllowOtherCardType() {
        val changeOfPlans = new SkillCard(5, CHANGE_OF_PLANS);
        player(1).gainSkillCards(changeOfPlans);

        assertInvalid(new DeclareEmergencyEvent(1, changeOfPlans));
    }
}
