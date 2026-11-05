package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;

public class KeepTabsOnVisitorFailEventProcessor extends EventProcessor<KeepTabsOnVisitorFailEvent> {

    @Override
    public Followup process() {
        return ops.rollDie() <= 4
                ? single(new LoseResourceEvent(POPULATION, 2))
                : NONE;
    }
}
