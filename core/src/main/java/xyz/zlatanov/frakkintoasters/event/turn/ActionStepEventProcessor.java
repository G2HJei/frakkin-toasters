package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.Actions;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTION;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.EXPERT_PILOT;

public class ActionStepEventProcessor extends EventProcessor<ActionStepEvent> {

    // The current player takes an action, e.g. his location's or a Skill Card's, a pilot may activate his viper instead,
    // and either may do nothing (rule 8.4).
    @Override
    public Followup process() {
        if (game.turn().endedEarly()) {
            return NONE;
        }
        ops.changeStep(ACTION);
        val current = game.currentPlayer().number();
        // Expert Pilot: a second action, when she started her turn piloting.
        val actions = game.turn().hasUsed(current, EXPERT_PILOT)
                ? all(Actions.offerAction(game, current), single(new GrantEvent(current, Grant.ACTION, GrantedBy.EXPERT_PILOT)))
                : Actions.offerAction(game, current);
        return EndOfActionStepEventProcessor.actsThen(game.currentPlayer())
                ? all(actions, single(new EndOfActionStepEvent()))
                : actions;
    }
}
