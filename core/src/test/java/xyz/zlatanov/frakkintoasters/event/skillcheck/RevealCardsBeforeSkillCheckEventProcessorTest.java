package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

class RevealCardsBeforeSkillCheckEventProcessorTest extends EventTestHarness<RevealCardsBeforeSkillCheckEvent> {

    @Test
    void shouldFollowUpWithDestinyDeckWhenNoCardPlayed() {
        execute(new RevealCardsBeforeSkillCheckEvent());
        assertFollowup(new PlayFromDestinyDeckEvent());
    }

}