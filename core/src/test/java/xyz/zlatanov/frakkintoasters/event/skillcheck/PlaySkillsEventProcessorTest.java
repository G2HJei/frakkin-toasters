package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CALCULATIONS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheck.ADMIRALS_QUARTERS;

class PlaySkillsEventProcessorTest extends EventTestHarness<PlaySkillsEvent> {

    @BeforeEach
    void setUp() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldContributeToActiveSkillCheck() {
        val contribution = new SkillCard(0, CALCULATIONS);
        execute(new PlaySkillsEvent(1, contribution));
        assertTrue(game.activeSkillCheck().cards().cards().contains(contribution));
    }
}