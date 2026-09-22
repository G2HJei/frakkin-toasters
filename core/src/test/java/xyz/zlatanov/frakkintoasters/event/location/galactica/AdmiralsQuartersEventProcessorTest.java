package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheck.ADMIRALS_QUARTERS;

class AdmiralsQuartersEventProcessorTest extends EventTestHarness<AdmiralsQuartersEvent> {

    @Test
    void shouldActivateAdmiralsQuartersSkillCheck() {
        game.currentPlayer(2);
        execute(new AdmiralsQuartersEvent(2, 3));
        assertEquals(ADMIRALS_QUARTERS, game.activeSkillCheck());
        assertSkillCheckTriggered();
    }

}