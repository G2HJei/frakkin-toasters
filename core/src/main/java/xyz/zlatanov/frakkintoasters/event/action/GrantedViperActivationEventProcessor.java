package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class GrantedViperActivationEventProcessor extends EventProcessor<GrantedViperActivationEvent> {

    // Never one another player is piloting (rule 11.1). The viper is in space, so it isn't launched.
    @Override
    protected boolean isValid() {
        return isPlayer(event.playerNumber())
                && ActivateViperEventProcessor.isValidActivation(game, event.activation(),
                fighter -> fighter.pilot() == null || fighter.pilot() == player.character(), false);
    }

    @Override
    public Followup process() {
        return ActivateViperEventProcessor.activate(ops, event.playerNumber(), event.activation());
    }
}
