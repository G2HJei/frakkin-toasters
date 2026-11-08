package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoOpEventProcessorTest extends EventTestHarness<NoOpEvent> {

    @Test
    void changesNothing() {
        execute(new NoOpEvent(1));
        assertEquals(0, journal.facts().size());
    }
}
