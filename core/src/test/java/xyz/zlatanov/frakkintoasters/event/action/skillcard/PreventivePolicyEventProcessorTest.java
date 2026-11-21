package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.PREVENTIVE_POLICY;

class PreventivePolicyEventProcessorTest extends EventTestHarness<PreventivePolicyEvent> {

    private static final SkillCard PREVENTIVE_POLICY_3 = new SkillCard(3, PREVENTIVE_POLICY);

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(1).gainSkillCards(PREVENTIVE_POLICY_3);
    }

    @Test
    void theNextLossOfTheResourceIsReduced() {
        execute(new PreventivePolicyEvent(1, PREVENTIVE_POLICY_3, FOOD));

        assertEquals(Set.of(FOOD), game.turn().lossReductions());
        assertNoSkillCards(1);
    }

    @Test
    void theResourceHasToBeNamed() {
        assertInvalid(new PreventivePolicyEvent(1, PREVENTIVE_POLICY_3, null));
    }
}
