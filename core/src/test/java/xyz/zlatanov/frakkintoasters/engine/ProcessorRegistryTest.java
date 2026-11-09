package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEventProcessor;

import static org.junit.jupiter.api.Assertions.*;

class ProcessorRegistryTest {

    @Test
    void anEventFindsTheProcessorNamedAfterIt() {
        assertInstanceOf(NoOpEventProcessor.class, ProcessorRegistry.create(new NoOpEvent(1)));
    }

    @Test
    void everyEventGetsANewProcessor() {
        val event = new NoOpEvent(1);
        assertNotSame(ProcessorRegistry.create(event), ProcessorRegistry.create(event));
    }

}
