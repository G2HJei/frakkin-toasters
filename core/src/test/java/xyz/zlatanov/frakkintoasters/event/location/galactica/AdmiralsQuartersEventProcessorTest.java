package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class AdmiralsQuartersEventProcessorTest extends EventTestHarness<AdmiralsQuartersEvent> {

    @Test
    void shouldActivateAdmiralsQuartersSkillCheck() {
        execute(new AdmiralsQuartersEvent(1, 3));
        assertSkillCheckTriggered(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldRememberTheChosenCharacter() {
        execute(new AdmiralsQuartersEvent(1, 3));
        assertSkillCheckTriggered(ADMIRALS_QUARTERS);
        assertEquals(3, game.activeSkillCheck().targetPlayerNumber());
    }

}
