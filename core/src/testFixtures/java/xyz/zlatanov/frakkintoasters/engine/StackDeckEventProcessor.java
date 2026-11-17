package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.operation.TestOps;

public class StackDeckEventProcessor extends EventProcessor<StackDeckEvent> {

    @Override
    public Followup process() {
        TestOps.stackDeck(ops, event.deck(), event.topFirst());
        return Followup.NONE;
    }
}
