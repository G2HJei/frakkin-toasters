package xyz.zlatanov.frakkintoasters.engine;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

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

    @Test
    void anEventWithoutAProcessorFailsEvenWithoutAsserts() {
        // asserts are off in production, so a missing processor would be a NullPointerException
        assertThrows(FrakCallTheAdmiralException.class, () -> ProcessorRegistry.create(new PlayQuorumCardEvent()));
    }

}
