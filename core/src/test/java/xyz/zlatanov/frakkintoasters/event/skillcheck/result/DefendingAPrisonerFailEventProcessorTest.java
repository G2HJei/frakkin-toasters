package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

class DefendingAPrisonerFailEventProcessorTest extends EventTestHarness<DefendingAPrisonerFailEvent> {

    @Test
    void shouldRollADie() {
        execute(new DefendingAPrisonerFailEvent());

        assertFollowup(rollDie(new DefendingAPrisonerFailResultEvent()));
    }
}
