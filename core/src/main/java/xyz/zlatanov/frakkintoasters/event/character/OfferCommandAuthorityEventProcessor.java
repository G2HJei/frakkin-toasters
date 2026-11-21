package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class OfferCommandAuthorityEventProcessor extends EventProcessor<OfferCommandAuthorityEvent> {

    @Override
    protected boolean isValid() {
        return game.activeSkillCheck() != null;
    }

    @Override
    public Followup process() {
        // What the result did may have changed who can use it, e.g. an execution.
        return CommandAuthorityEventProcessor.offer(game);
    }
}
