package xyz.zlatanov.frakkintoasters.event;

import xyz.zlatanov.frakkintoasters.EventProcessor;

import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.activate;
import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.isValidActivation;

public class ActivateUnmannedViperEventProcessor extends EventProcessor<ActivateUnmannedViperEvent> {

    @Override
    protected boolean isValid() {
        return isValidActivation(game, event.activation(), fighter -> fighter.pilot() == null, true);
    }

    @Override
    public Followup process() {
        return activate(ops, event.playerNumber(), event.activation());
    }
}
