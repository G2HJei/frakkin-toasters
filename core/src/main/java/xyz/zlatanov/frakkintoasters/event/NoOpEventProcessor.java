package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.EventProcessor;

// Does nothing, so "do nothing" is an ordinary option and the engine needs no special case for it.
public class NoOpEventProcessor extends EventProcessor<NoOpEvent> {

    @Override
    public Followup process() {
        return Followup.NONE;
    }
}
