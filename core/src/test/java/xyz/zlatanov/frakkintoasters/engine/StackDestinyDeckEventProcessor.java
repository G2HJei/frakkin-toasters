package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.operation.TestOps;

public class StackDestinyDeckEventProcessor extends EventProcessor<StackDestinyDeckEvent> {

    @Override
    public Followup process() {
        TestOps.stackDestinyDeck(ops, event.topFirst());
        return Followup.NONE;
    }
}
