package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.DamageGalacticaEvent;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.POWER_FAILURE;

class ResolveConsequencesEventProcessorTest extends EventTestHarness<ResolveConsequencesEvent> {

    @Test
    void shouldDamageGalacticaOnPowerFailure() {
        game.startSkillCheck(POWER_FAILURE);

        execute(new ResolveConsequencesEvent());

        assertFollowup(new DamageGalacticaEvent());
    }
}
