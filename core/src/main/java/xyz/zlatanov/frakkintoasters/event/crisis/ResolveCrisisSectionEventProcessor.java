package xyz.zlatanov.frakkintoasters.event.crisis;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class ResolveCrisisSectionEventProcessor extends EventProcessor<ResolveCrisisSectionEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null && event.section() != null;
    }

    @Override
    public Followup process() {
        return ChooseCrisisSectionEventProcessor.resolve(game, ops, event.card(), event.section());
    }
}
