package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class QuorumInUproarFailEventProcessor extends EventProcessor<QuorumInUproarFailEvent> {

    @Override
    public Followup process() {
        ops.titles().discardRandomQuorumCards(2);
        return Followup.NONE;
    }
}
