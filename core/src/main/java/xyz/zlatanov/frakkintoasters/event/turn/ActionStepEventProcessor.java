package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.Actions;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTION;

public class ActionStepEventProcessor extends EventProcessor<ActionStepEvent> {

    // The current player takes an action, e.g. his location's or a Skill Card's, a pilot may activate his viper instead,
    // and either may do nothing (rule 8.4).
    @Override
    public Followup process() {
        if (game.turn().endedEarly()) {
            return NONE;
        }
        ops.changeStep(ACTION);
        return Actions.offerAction(game, game.currentPlayer().number());
    }
}
