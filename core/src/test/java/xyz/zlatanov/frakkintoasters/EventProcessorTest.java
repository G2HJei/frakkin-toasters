package xyz.zlatanov.frakkintoasters;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.exception.InvalidActionException;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

class EventProcessorTest extends TestHarness {

    @Test
    void prepareValidatesWithoutOpsAndRunProcessesWithThem() {
        val processor = new TestEventProcessor(true);

        processor.prepare(game, new NoOpEvent(1));

        assertTrue(processor.validated);
        assertNull(processor.opsWhileValidating);
        assertNull(processor.opsWhileProcessing);

        assertEquals(single(new NoOpEvent(2)), processor.run(ops));
        assertSame(ops, processor.opsWhileProcessing);
    }

    @Test
    void aRejectedEventIsNeverProcessed() {
        val processor = new TestEventProcessor(false);

        assertThrows(InvalidActionException.class, () -> processor.prepare(game, new NoOpEvent(1)));
        assertThrows(FrakCallTheAdmiralException.class, () -> processor.run(ops));
        assertNull(processor.opsWhileProcessing);
    }

    @Test
    void runNeedsAPreparedEvent() {
        assertThrows(FrakCallTheAdmiralException.class, () -> new TestEventProcessor(true).run(ops));
    }

    @Test
    void aProcessorRunsOneEventOnce() {
        val processor = new TestEventProcessor(true);
        processor.prepare(game, new NoOpEvent(1)).run(ops);

        assertThrows(FrakCallTheAdmiralException.class, () -> processor.run(ops));
        assertThrows(FrakCallTheAdmiralException.class, () -> processor.prepare(game, new NoOpEvent(1)));
    }

    private static class TestEventProcessor extends EventProcessor<NoOpEvent> {

        private final boolean valid;
        private       boolean validated;
        private       GameOps opsWhileValidating;
        private       GameOps opsWhileProcessing;

        TestEventProcessor(boolean valid) {
            this.valid = valid;
        }

        @Override
        protected boolean isValid() {
            validated = true;
            opsWhileValidating = ops;
            return valid;
        }

        @Override
        public Followup process() {
            opsWhileProcessing = ops;
            return single(new NoOpEvent(2));
        }
    }
}
