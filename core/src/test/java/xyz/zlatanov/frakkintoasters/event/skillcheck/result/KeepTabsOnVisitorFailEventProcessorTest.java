package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

class KeepTabsOnVisitorFailEventProcessorTest extends EventTestHarness<KeepTabsOnVisitorFailEvent> {

    @Test
    void shouldRollADie() {
        execute(new KeepTabsOnVisitorFailEvent());

        assertFollowup(rollDie(new KeepTabsOnVisitorFailResultEvent()));
    }
}
