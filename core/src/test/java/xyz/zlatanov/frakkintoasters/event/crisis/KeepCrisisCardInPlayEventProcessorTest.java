package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.DETECTOR_SABOTAGE;

class KeepCrisisCardInPlayEventProcessorTest extends EventTestHarness<KeepCrisisCardInPlayEvent> {

    @Test
    void shouldKeepTheCardInPlay() {
        execute(new KeepCrisisCardInPlayEvent(DETECTOR_SABOTAGE));

        assertEquals(List.of(DETECTOR_SABOTAGE), game.crisisCardsInPlay());
    }

    @Test
    void shouldNotKeepNoCard() {
        assertInvalid(new KeepCrisisCardInPlayEvent(null));
    }
}
